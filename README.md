# 禁制录 Android Demo

一个使用 Kotlin + Jetpack Compose 搭建的最小修仙禁制解谜框架。

## 当前功能

- 单 Activity Compose 架构
- 第一关「引灵」5×5 阵盘
- 点击节点形成灵力路径
- 必须从入口开始，只能走上下左右，抵达阵眼即破解
- ViewModel 管理关卡状态
- 独立 SealEngine 规则层，便于后续添加符文与复杂规则

## 运行

1. 使用 Android Studio 打开项目根目录 `ForbiddenSeal`
2. 等待 Gradle Sync 完成
3. 选择 Android 8.0（API 26）以上模拟器或设备
4. 点击 Run

## 推荐的下一步

- 第二关加入障碍节点，强化“相邻路径”规则
- 第三关加入第一个未知符文「聚灵」
- 增加失败反馈动画、音效和玩家禁制图录
- 将关卡数据迁移为 JSON，方便快速编辑
