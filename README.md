# DropSauce Offline

本分支基于 DropSauce v1.1.2，使用 Android 文档选择器导入本地漫画和 EPUB。书库、收藏、书签、阅读进度和手动备份保存在设备上。朗读使用用户设置的系统 TTS 引擎和已安装的离线语音。书内指向网站的插图会跳过。

在配有 Android SDK 37 的 Windows 电脑上运行 `gradlew.bat :app:assemblePreview`。生成的本机测试包位于 `app/build/outputs/apk/preview/`，包名为 `org.haziffe.dropsauce.preview`，使用本机 Android 调试密钥签名。可以与上游版本并存。更新测试包时须使用相同的签名密钥。

## Credits

DropSauce exists because of the work already done by the open-source Android manga reader community.

Special thanks to the original [Kotatsu](https://github.com/KotatsuApp/Kotatsu) developers, [LNReader](https://github.com/LNReader/lnreader) developers, and the [Mihon](https://github.com/mihonapp/mihon) developers/community for the ideas, code, source ecosystem, and long-running maintenance work that helped shape projects like this.

## License

[![GNU GPLv3 Image](https://www.gnu.org/graphics/gplv3-127x51.png)](http://www.gnu.org/licenses/gpl-3.0.en.html)

<div align="left">

All programs from DropSauce™ project are free, open-source programs under the GPL license. You may copy, distribute, and modify the software as long as you keep track of changes/dates in the source files. Any modifications to the software, including code licensed under the GPL (via a compiler), must also be provided under the GPL license.

</div>

## Disclaimer

<div align="left">

The developer(s) of this application do not have any affiliation with the content providers available. If there is any content, it is provided by external libraries added or imported by users; the application itself does not include any built-in content.

</div>
