# DropSauce 离线版逐 Activity 核查

候选代码 fdb2ed44。以 Preview 合并清单为准，包含 31 个 Activity。测试环境为电脑上的 DropAudit，Android API 37。运行状态栏区分最终包截图、退出型兼容入口、安全入口及依赖核查。需要复现内部非导出页面时，测试工具在本机模拟器内使用同签名 instrumentation 注入本地书、样页或临时分配的小组件 ID。书架、详情、搜索、阅读设置另通过用户入口验证。

[六页实测总览](overview.jpg) · [改版前后对照](comparison.jpg)

| Activity | 页面 | 运行证据 | 设计与处理 |
|---|---|---|---|
| MainActivity | 本地书架 | [最终包画面](screens/MainActivity.jpg) | 正文、导航和筛选有各自的稳定位置。延用上轮收藏/历史筛选边界和 1× 转场；本轮检查已有封面和占位书封同时陈列。 |
| DetailsExpressiveActivity | 本地书籍详情 | [最终包画面](screens/DetailsExpressiveActivity.jpg) | 书封、书名和阅读操作承担主要层级，章节按钮在阅读按钮旁。实际书籍进入后检查进度和文件信息。 |
| ReaderActivity | 漫画阅读 | [最终包画面](screens/ReaderActivity.jpg) | 无工具栏时画面交给漫画；通过两页测试漫画检查实际内容。EPUB 的列宽、系统朗读和正文滑动见上轮验收。 |
| MangaListActivity | 本地来源列表 | [最终包画面](screens/MangaListActivity.jpg) | 指定 LOCAL 后显示书封网格及导入操作。同一书封语言可以沿用；工具栏需同时保留来源、返回和导入。 |
| UpdatesActivity | 旧更新记录 | [最终包画面](screens/UpdatesActivity.jpg) | 原空状态错误指向已移除的“浏览”页。改为单行“暂无章节更新记录”，并保留已有记录的列表。 |
| FavouritesActivity | 独立收藏列表 | [最终包画面](screens/FavouritesActivity.jpg) | 独立窗口继承折叠标题和筛选控件。空筛选仍能直接重置；与主界面里的收藏 Fragment 共用内容。 |
| AllBookmarksActivity | 独立书签列表 | [最终包画面](screens/AllBookmarksActivity.jpg) | 空状态使用固定鸟图；有摘录时沿用文本书签卡。此次检查空列表，未新建或删除用户书签。 |
| SuggestionsActivity | 本地推荐 | [最终包画面](screens/SuggestionsActivity.jpg) | 观察到阅读数据不足时的空页，文案指向开始阅读。真实推荐排序质量未在此轮评估。 |
| RelatedMangaActivity | 关联书籍 | [最终包画面](screens/RelatedMangaActivity.jpg) | 携带测试漫画对象进入；没有关联书时显示空状态。未制造假推荐填充版面。 |
| SettingsActivity | 设置入口 | [最终包画面](screens/SettingsActivity.jpg) | 核查阅读、外观、统计、存储与备份的分组。补齐中文阅读分组标题；从正常设置入口走到阅读触区编辑器。 |
| ReaderTapGridConfigActivity | 阅读触区 | [最终包画面](screens/ReaderTapGridConfigActivity.jpg) | 九格过去重复两套标签、同时呈现两个动作。现在点按与长按分段切换，每格只展示当前手势的动作；前翻、后翻、工具和未分配使用对比配色。大字号可滚动，点格选动作，原有长按快捷编辑仍可用。 |
| MangaDirectoriesActivity | 目录管理 | [最终包画面](screens/MangaDirectoriesActivity.jpg) | 旧进度条把文件夹大小与剩余空间相除，无法表达磁盘使用率。改为明确显示目录实际大小和设备可用容量，突出容量、弱化路径，私有目录保留卸载提示。 |
| WebViewActivity | 扩展兼容入口 | 实测立即退出 | 原先传入网址会转发到未注册的 BrowserActivity。离线版收到旧 Intent 后直接返回取消。 |
| SourceAuthActivity | 旧来源登录入口 | 实测立即退出 | 原先先创建 WebView 再退出。现在直接返回取消，保留旧调用契约。 |
| FavouriteCategoriesActivity | 分类管理 | [最终包画面](screens/FavouriteCategoriesActivity.jpg) | 保留书封、编辑和拖动排序。分类名可占两行，编辑与拖动区域至少 48dp；删除离线无效的跟踪、下载徽标。 |
| SearchActivity | 本地搜索结果 | [最终包画面](screens/SearchActivity.jpg) | 从书库提交查询，最终截图记录搜索结果页的加载态。命中与无结果的画面见上一轮阅读流程审计。 |
| ProtectActivity | 应用解锁 | 安全入口生命周期；未认证 | 源码使用安全窗口、PIN 或系统认证。模拟器检查入口生命周期，未建立新密码或录制认证界面，真实指纹流程未测。 |
| DownloadsActivity | 历史下载任务 | [最终包画面](screens/DownloadsActivity.jpg) | 观察到空任务页；保留已有任务信息和本地历史。该页无测试下载进度，不能把空页当成下载链路验证。 |
| ImageActivity | 单图查看 | [最终包画面](screens/ImageActivity.jpg) | 用本地测试图验证大幅预览、返回、保存与分享入口。未执行对外分享。 |
| CrashDialogActivity | 本地异常对话框 | [旧包异常画面](screens/CrashDialogActivity.jpg) | 核查测试中缺少样页参数产生的真实异常提示，包含错误摘要、复制和关闭。关闭后重试；交付包另外修正触发该异常的入口。 |
| FavouritesCategoryEditActivity | 分类编辑 | [最终包画面](screens/FavouritesCategoryEditActivity.jpg) | 保留名称、排序和书架可见性。移除“检查更新”“下载新章节”两项在线选项，新分类关闭相关标志，编辑已有分类保留兼容字段。 |
| OverrideConfigActivity | 书籍信息和封面编辑 | [最终包画面](screens/OverrideConfigActivity.jpg) | 原四格将不能使用的网络 URL 与本地来源并列。改为相册、图片文件、漫画页三个入口；没有关联记录时隐藏导入动作。补齐中文封面标签。 |
| ColorFilterConfigActivity | 颜色校正 | [最终包画面](screens/ColorFilterConfigActivity.jpg) | 有书和样页时检查预览、亮度、对比度、反色、灰度、护眼和重置。另测缺少输入的调用；旧包进入崩溃提示，交付包提前退出。 |
| StatsActivity | 阅读统计 | [最终包画面](screens/StatsActivity.jpg) | 单个时间段原会画成一块铺满整卡的色块。柱宽现在最多 26dp，短柱按阅读时长比例绘制，点按仍按完整时间槽命中。补齐时段、天数、排行和速度的中文标签。 |
| AlternativesActivity | 旧替代来源 | [最终包画面](screens/AlternativesActivity.jpg) | 本地书没有候选时展示空列表；归为旧记录辅助页。此轮未迁移来源。 |
| TrackerDebugActivity | 旧跟踪日志 | [最终包画面](screens/TrackerDebugActivity.jpg) | 用恢复的本地测试记录检查调试列表。该页行为是查看既有条目，没有触发网络跟踪。 |
| PageImagePickActivity | 从书中选图 | [最终包画面](screens/PageImagePickActivity.jpg) | 选择入口按历史与收藏分组展示封面，适合识别书名和图片。第一次选择书、再选择页，两层保留同一个返回路径。 |
| HistoryWidgetConfigActivity | 历史小组件配置 | [最终包画面](screens/HistoryWidgetConfigActivity.jpg) | 用测试宿主分配的 widget ID 打开，按网格或列表二选一。保留单选对话框，补齐中文标题和选项。 |
| FavoritesWidgetConfigActivity | 收藏小组件配置 | [最终包画面](screens/FavoritesWidgetConfigActivity.jpg) | 选中态叠加对勾和主题色背景，空封面改用书架书封。固定保存栏已单独占位，去掉列表底部重复的 96dp 留白。核查中文固定数量。 |
| FingerprintDialogActivity | AndroidX 生物识别 | 依赖实现与调用配置 | 检查合并清单和所用 biometric AAR 的 onCreate、系统认证与对话框实现。由认证库控制，未做真实硬件录入或验证。 |
| CrashReportDialog | ACRA 默认对话框 | 依赖实现与调用配置 | 检查依赖类及运行配置；BaseApp 将 reportDialogClass 指向 CrashDialogActivity。默认类仍在合并清单，实际异常由定制页展示。 |

**视觉取舍。** 把识别任务放在首位：手势编辑器用位置和色彩区分动作，目录用容量数字，书籍选择用封面，阅读页给正文让出空间。仍有两类可见不足：旧的推荐、关联、替代来源窗口在无数据时构图相近；少数沿用的菜单和排序文案在简体中文环境仍会出现英文。图库按同一模拟器比例保存，供继续比较。

**测试边界。** 进入页面、检查视觉层级和核对按钮语义，不能代替每个按钮的完整业务回归。未测物理指纹、所有语言、全部屏幕尺寸和帧率。系统安全窗口未进行截图。

最终包 fdb2ed44 通过本地 Preview 构建、204 项单测和关键 lint。模拟器安装包与交付 APK 的 SHA-256 均为 e86a973f28e1320b03a7b13651b8be2fea4bdb3c517a663b19bd0b596af53bdd，未声明 INTERNET 权限。25 张图来自最终包。异常对话框另附修正前的诊断画面。小组件测试选择一项后计数从 0/3 变为 1/3，退出时未保存测试选择，测试宿主及 instrumentation 已清理。
