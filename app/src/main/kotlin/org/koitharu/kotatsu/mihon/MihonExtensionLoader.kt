package org.koitharu.kotatsu.mihon

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.koitharu.kotatsu.core.logs.breadcrumb
import org.koitharu.kotatsu.mihon.model.MihonExtensionInfo
import org.koitharu.kotatsu.mihon.model.MihonLoadResult
import eu.kanade.tachiyomi.util.lang.Hash
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import org.koitharu.kotatsu.core.model.ContentWarning

@Singleton
class MihonExtensionLoader @Inject constructor(

) {

	/**
	 * Like Mihon, an extension whose APK hasn't changed is reused rather than re-created on every
	 * reload (any app install/update on the device, private-mode toggles, pull-to-refresh...). A
	 * fresh instance threw away each source's client, rate limiter and in-memory session state,
	 * repeated all class loading while details/covers waited, and left the old copy alive in
	 * cached repositories next to the new one.
	 */
	private val loadedExtensions = ConcurrentHashMap<String, LoadedExtension>()

	private class LoadedExtension(val apkPath: String?, val result: MihonLoadResult.Success)

	// Installed extension packages, reused until the system reports a package change (see getInstalledExtensionPackages).
	private var cachedExtensionPackages: List<PackageInfo>? = null
	private var packagesSequence = 0

	companion object {
		private const val TAG = "MihonExtensionLoader"

		// Opening class loaders and creating sources is CPU-heavy. One coroutine per extension on the
		// plain IO pool ran dozens at once at launch, starving the UI thread while the first screens drew.
		private val loadDispatcher = Dispatchers.IO.limitedParallelism(
			(Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1),
		)

		private const val EXTENSION_FEATURE = "tachiyomi.extension"

		/**
		 * Tsundoku's novel extensions are ordinary Tachiyomi extension APKs that declare a different
		 * feature and put their manifest metadata under a matching namespace. Everything after
		 * discovery is identical — the source itself declares `isNovelSource`.
		 */
		private const val EXTENSION_FEATURE_NOVEL = "tachiyomi.novelextension"
		private val EXTENSION_FEATURES = setOf(EXTENSION_FEATURE, EXTENSION_FEATURE_NOVEL)

		/** `<namespace>.class` / `.factory` / `.nsfw`, in the order they're tried. */
		private val METADATA_NAMESPACES = listOf(EXTENSION_FEATURE, EXTENSION_FEATURE_NOVEL)
		private val METADATA_SOURCE_CLASS_KEYS = METADATA_NAMESPACES.map { "$it.class" }
		private val METADATA_SOURCE_FACTORY_KEYS = METADATA_NAMESPACES.map { "$it.factory" }
		private val METADATA_NSFW_KEYS = METADATA_NAMESPACES.map { "$it.nsfw" }
		private const val METADATA_EXTENSION_LIB = "tachiyomix.extensionLib"
		private const val METADATA_CONTENT_WARNING = "tachiyomix.contentWarning"
		/** Languages hidden from the whole app: their source variants are never loaded. */
		val HIDDEN_LANGUAGES = setOf("he", "iw")

		@Suppress("DEPRECATION")
		fun getSignatures(pkgInfo: PackageInfo): List<String> {
			val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				val signingInfo = pkgInfo.signingInfo ?: return emptyList()
				if (signingInfo.hasMultipleSigners()) {
					signingInfo.apkContentsSigners
				} else {
					signingInfo.signingCertificateHistory
				}
			} else {
				pkgInfo.signatures
			}
			return signatures.orEmpty().map { Hash.sha256(it.toByteArray()) }
		}

		/** SHA-256 signatures of one installed package — for repo attribution by fingerprint. */
		@Suppress("DEPRECATION")
		fun getPackageSignatures(context: Context, pkgName: String): List<String> = runCatching {
			val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
				PackageManager.GET_SIGNING_CERTIFICATES
			} else {
				PackageManager.GET_SIGNATURES
			}
			getSignatures(context.packageManager.getPackageInfo(pkgName, flags))
		}.getOrDefault(emptyList())
		// Keep the accepted ABI window bounded by Mihon's source-api. Loading a hypothetical newer
		// APK and hoping its missing host symbols are unused turns a clear incompatibility into a
		// delayed NoSuchMethodError.
		const val LIB_VERSION_MIN = 1.4
		const val LIB_VERSION_MAX = 1.6

		private const val PRIVATE_EXTENSION_DIR = "exts"
		private const val PRIVATE_EXTENSION_EXT = "ext"

		fun getPrivateExtensionDir(context: Context): File =
			File(context.filesDir, PRIVATE_EXTENSION_DIR)

		fun isPrivateExtensionInstalled(context: Context, pkgName: String): Boolean =
			File(getPrivateExtensionDir(context), "$pkgName.$PRIVATE_EXTENSION_EXT").isFile

		fun hasPrivateExtensions(context: Context): Boolean {
			val dir = getPrivateExtensionDir(context)
			return dir.listFiles()?.any { it.isFile && it.extension == PRIVATE_EXTENSION_EXT } == true
		}

		fun installPrivateExtensionFile(
			context: Context,
			file: File,
			replaceExistingProvider: Boolean = false,
			expectedPackageName: String? = null,
		): Boolean = false

		fun uninstallPrivateExtension(context: Context, pkgName: String) {
			File(getPrivateExtensionDir(context), "$pkgName.$PRIVATE_EXTENSION_EXT").delete()
		}

		private fun copyAndSetReadOnly(source: File, target: File) {
			target.parentFile?.mkdirs()
			source.inputStream().use { input ->
				target.outputStream().use { output ->
					input.copyTo(output)
				}
			}
			target.setReadOnly()
		}

		@Suppress("DEPRECATION")
		private val packageFlags: Int
			get() = PackageManager.GET_META_DATA or
				PackageManager.GET_CONFIGURATIONS or
				PackageManager.GET_SIGNATURES or
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) PackageManager.GET_SIGNING_CERTIFICATES else 0

		fun isExtensionPackage(context: Context, pkgName: String): Boolean = try {
			@Suppress("DEPRECATION")
			val pkgInfo = context.packageManager.getPackageInfo(
				pkgName,
				PackageManager.GET_META_DATA or PackageManager.GET_CONFIGURATIONS,
			)
			isPackageAnExtensionStatic(pkgInfo)
		} catch (_: PackageManager.NameNotFoundException) {
			false
		}

		internal fun isPackageAnExtensionStatic(pkgInfo: PackageInfo): Boolean {
			val appInfo = pkgInfo.applicationInfo ?: return false
			val hasFeature = pkgInfo.reqFeatures?.any { it.name in EXTENSION_FEATURES } == true
			return hasFeature || readSourceClassNames(appInfo.metaData) != null
		}

		/** The `;`-separated source class list, from whichever namespace the APK declares it under. */
		internal fun readSourceClassNames(metaData: Bundle?): String? {
			metaData ?: return null
			return METADATA_SOURCE_CLASS_KEYS.firstNotNullOfOrNull { metaData.getString(it) }
				?: METADATA_SOURCE_FACTORY_KEYS.firstNotNullOfOrNull { metaData.getString(it) }
		}

		internal fun normalizeSourceClassNames(pkgName: String, sourceClassNames: String): List<String> {
			return sourceClassNames
				.split(';', ':', ',')
				.map { it.trim() }
				.filter { it.isNotEmpty() }
				.map { className ->
					if (className.startsWith('.')) {
						pkgName + className
					} else {
						className
					}
				}
		}

		/**
		 * Names to try for one declared source class, in order. Some novel-extension repos declare
		 * their classes under the fork's own package while the compiled class kept the upstream
		 * name, so the upstream spelling is tried as a fallback — same shim Tsundoku's loader has.
		 */
		internal fun sourceClassCandidates(className: String): List<String> {
			val upstream = className.replace(TSUNDOKU_CLASS_PREFIX, MIHON_CLASS_PREFIX)
			return if (upstream == className) listOf(className) else listOf(className, upstream)
		}

		private const val TSUNDOKU_CLASS_PREFIX = "app.tsundoku.extension."
		private const val MIHON_CLASS_PREFIX = "eu.kanade.tachiyomi.extension."

		/** Newer extensions declare a [ContentWarning] (1 = mixed, 2 = 18+); older ones only an nsfw flag. */
		internal fun readContentWarning(metaData: Bundle): ContentWarning {
			if (metaData.containsKey(METADATA_CONTENT_WARNING)) {
				return when (metaData.getInt(METADATA_CONTENT_WARNING, 0)) {
					1 -> ContentWarning.MIXED
					2 -> ContentWarning.NSFW
					else -> ContentWarning.SAFE
				}
			}
			return if (readNsfwFlag(metaData)) ContentWarning.NSFW else ContentWarning.SAFE
		}

		private fun readNsfwFlag(metaData: Bundle): Boolean {
			val key = METADATA_NSFW_KEYS.firstOrNull { metaData.containsKey(it) } ?: return false
			return runCatching {
				parseNsfwFlag(metaData.getInt(key))
			}.getOrElse {
				runCatching {
					parseNsfwFlag(metaData.getBoolean(key))
				}.getOrElse {
					parseNsfwFlag(metaData.getString(key))
				}
			}
		}

		internal fun parseNsfwFlag(value: Any?): Boolean {
			return when (value) {
				is Boolean -> value
				is Int -> value != 0
				is String -> value == "1" || value.equals("true", ignoreCase = true)
				else -> false
			}
		}

		/**
		 * Only a real Double from the manifest is used; anything else (manifest decimals are always
		 * Float) falls back to the version name, which is what getDouble's 0.0 default did before.
		 * Never widen the Float: 1.4f.toDouble() is 1.39999…, outside the supported range.
		 */
		internal fun parseLibVersion(metaValue: Any?, versionName: String): Double? =
			(metaValue as? Double)?.takeUnless { it == 0.0 }
				?: versionName.substringBeforeLast('.').toDoubleOrNull()
				?: versionName.split('.').take(2).joinToString(".").toDoubleOrNull()

		internal fun isSupportedLibVersion(libVersion: Double): Boolean {
			return libVersion in LIB_VERSION_MIN..LIB_VERSION_MAX
		}
	}

	suspend fun loadExtensions(context: Context, privateMode: Boolean = false): List<MihonLoadResult> = emptyList()

	suspend fun loadExtension(context: Context, packageName: String): MihonLoadResult? = null

	fun getInstalledExtensions(context: Context, privateMode: Boolean = false): List<MihonExtensionInfo> = emptyList()

	private fun ApplicationInfo.fixBasePaths(apkPath: String) {
		if (sourceDir == null) sourceDir = apkPath
		if (publicSourceDir == null) publicSourceDir = apkPath
	}
}
