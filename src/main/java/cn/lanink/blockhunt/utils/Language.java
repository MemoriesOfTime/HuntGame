package cn.lanink.blockhunt.utils;

import cn.nukkit.utils.Config;

public class Language {

    //控制台
    public String scoreboardAPINotFound = "§c请安装计分板前置！";
    public String defaultSkinSuccess = "§a 默认尸体皮肤加载完成";
    public String defaultSkinFailure = "§c 默认尸体皮肤加载失败！请检查插件完整性！";
    public String startLoadingRoom = "§e开始加载房间...";
    public String roomLoadedSuccess = "§a房间：%name% 已加载！";
    public String roomLoadedFailureByConfig = "§c房间：%name% 配置不完整，加载失败！";
    public String roomLoadedFailureByLevel = "§c房间：%name% 地图读取失败！";
    public String roomLoadedAllSuccess = "§e房间加载完成！当前已加载 %number% 个房间！";
    public String roomUnloadSuccess = "§c房间：%name% 已卸载！";
    public String roomUnloadFailure = "§c房间：%name% 非正常结束！";
    public String pluginEnable = "§e插件加载完成！欢迎使用！";
    public String pluginDisable = "§c插件卸载完成！";
    //命令
    public String useCmdInRoom = "§e >> §c游戏中无法使用其他命令";
    public String cmdHelp = "§a查看帮助：/%cmdName% help";
    public String userHelp = "§eMurderMystery--命令帮助 \n " +
            "§a/%cmdName% §e打开ui \n " +
            "§a/%cmdName% join 房间名称 §e加入游戏 \n " +
            "§a/%cmdName% quit §e退出游戏 \n " +
            "§a/%cmdName% list §e查看房间列表";
    public String noPermission = "§c你没有权限！";
    public String joinRoom = "§a你已加入房间: %name%";
    public String joinRoomOnRoom = "§c你已经在一个房间中了!";
    public String joinRoomOnRiding = "§a请勿在骑乘状态下进入房间！";
    public String joinRandomRoom = "§a已为你随机分配房间！";
    public String joinRoomIsPlaying = "§a该房间正在游戏中，请稍后";
    public String joinRoomIsFull = "§a该房间已满人，请稍后";
    public String joinRoomIsNotFound = "§a该房间不存在！";
    public String joinRoomNotAvailable = "§a暂无房间可用！";
    public String quitRoom = "§a你已退出房间";
    public String quitRoomNotInRoom = "§a你本来就不在游戏房间！";
    public String listRoom = "§e房间列表： §a %list%";
    public String useCmdInCon = "请在游戏内输入！";
    public String adminHelp = "§eMurderMystery--命令帮助 \n " +
            "§a/%cmdName% §e打开ui \n " +
            "§a/%cmdName% setwaitspawn §e设置当前位置为游戏出生点 \n " +
            "§a/%cmdName% addrandomspawn  §e添加当前位置为游戏等待出生点 \n " +
            "§a/%cmdName% addgoldspawn §e添加当前位置为金锭生成点 \n " +
            "§a/%cmdName% setgoldspawntime 数字 §e设置金锭生成间隔 \n " +
            "§a/%cmdName% setwaittime 数字 §e设置游戏人数足够后的等待时间 \n " +
            "§a/%cmdName% setgametime 数字 §e设置每轮游戏最长时间 \n " +
            "§a/%cmdName% setgamemode 模式 §e设置房间游戏模式 \n " +
            "§a/%cmdName% startroom §e开始所在地图的房间游戏 \n" +
            "§a/%cmdName% reloadroom §e重载所有房间 \n " +
            "§a/%cmdName% unloadroom §e关闭所有房间,并卸载配置";
    public String adminSetSpawn = "§a默认出生点设置成功！";
    public String adminAddRandomSpawn = "§a随机出生点添加成功！";
    public String adminNotNumber = "§a输入的参数不是数字！";
    public String adminSetWaitTime = "§a等待时间已设置为：%time%";
    public String adminSetGameTime = "§a游戏时间已设置为：%time%";
    public String adminSetGameTimeShort = "§a游戏时间最小不能低于1分钟！";
    public String adminStartNoPlayer = "§a房间人数不足三人,无法开始游戏！";
    public String adminLevelNoRoom = "§a当前地图不是游戏房间！";
    public String adminReload = "§a配置重载完成！请在后台查看信息！";
    public String adminUnload = "§a已卸载所有房间！请在后台查看信息！";
    public String roomSafeKick = "\n§c房间非正常关闭!\n为了您的背包安全，请稍后重进服务器！";
    public String preyChat = "§a[猎物] %player% + §b >>> %message%";
    public String huntersChat = "§a[猎人] %player% + §b >>> %message%";
    public String tpJoinRoomLevel = "§e >> §c要进入游戏地图，请先加入游戏！";
    public String tpQuitRoomLevel = "§e >> §c退出房间请使用命令！";
    //道具
    public String itemQuitRoom = "§c退出房间";
    public String itemQuitRoomLore = "手持点击,即可退出房间";
    //房间模式
    public String Classic = "经典";
    //身份
    public String prey = "猎物";
    public String hunters = "猎人";
    public String death = "死亡";
    //游戏提示信息
    public String titlePreyTitle = "§a猎物";
    public String titlePreySubtitle = "活下去，就是胜利";
    public String titleHuntersTitle = "§e猎人";
    public String titleHuntersSubtitle = "抓到所有藏起来的猎物";
    public String titleVictoryHuntersTitle = "§a猎人获得胜利！";
    public String titleVictoryPreySubtitle = "§a猎物获得胜利！";
    public String victoryHuntersBottom = "§e恭喜猎人获得胜利";
    public String victoryHuntersScoreBoard = "§e恭喜猎人获得胜利! ";
    public String victoryPreyBottom = "§e恭喜猎物获得胜利！";
    public String victoryPreyScoreBoard = "§e恭喜猎物获得胜利！";
    public String scoreBoardTitle = "§e方块躲猫猫";
    public String waitTimeScoreBoard = "玩家: §a %playerNumber%/16 \n §a开始倒计时： §l§e %time%";
    public String waitScoreBoard = "玩家: §a %playerNumber%/16 \n 最低游戏人数为 3 人 \n 等待玩家加入中";
    public String waitTimeBottom = "§a当前已有: %playerNumber% 位玩家 \n §a游戏还有: %time% 秒开始！";
    public String waitBottom = "§c等待玩家加入中,当前已有: %playerNumber% 位玩家";
    public String gameTimeScoreBoard = "§l§a当前身份:§e %mode% \n §l§a存活人数:§e %playerNumber% \n §l§a剩余时间:§e %time% §a秒 ";
    public String gameTimeBottom = "§a身份:§e %mode% \n §a距游戏结束还有:§e %time% §a秒\n当前还有:§e %playerNumber% §a人存活";
    public String huntersKillPrey = "%damagePlayer% 抓到了 %player%";

    //UI相关
    public String userMenuButton1 = "§e随机加入房间";
    public String userMenuButton2 = "§e退出当前房间";
    public String userMenuButton3 = "§e查看房间列表";
    public String adminMenuSetLevel = "当前设置地图：%name%";
    public String adminMenuButton1 = "§e设置默认出生点";
    public String adminMenuButton2 = "§e添加随机出生点";
    public String adminMenuButton3 = "§e设置时间参数";
    public String adminMenuButton4 = "§e重载所有房间";
    public String adminMenuButton5 = "§c卸载所有房间";
    public String adminTimeMenuInputText1 = "等待时间（秒）";
    public String adminTimeMenuInputText2 = "游戏时间（秒）";
    public String joinRoomOK = "§l§a确认要加入房间: %name% §l§a？";
    public String buttonOK = "§a确定";
    public String buttonReturn = "§c返回";


    public Language(Config config) {
        this.scoreboardAPINotFound = config.getString("scoreboardAPINotFound", this.scoreboardAPINotFound);
        this.defaultSkinSuccess = config.getString("defaultSkinSuccess", this.defaultSkinSuccess);
        this.defaultSkinFailure = config.getString("defaultSkinFailure", this.defaultSkinFailure);
        this.startLoadingRoom = config.getString("startLoadingRoom", this.startLoadingRoom);
        this.roomLoadedSuccess = config.getString("roomLoadedSuccess", this.roomLoadedSuccess);
        this.roomLoadedFailureByConfig = config.getString("roomLoadedFailureByConfig", this.roomLoadedFailureByConfig);
        this.roomLoadedFailureByLevel = config.getString("roomLoadedFailureByLevel", this.roomLoadedFailureByLevel);
        this.roomLoadedAllSuccess = config.getString("roomLoadedAllSuccess", this.roomLoadedAllSuccess);
        this.roomUnloadSuccess = config.getString("roomUnloadSuccess", this.roomUnloadSuccess);
        this.roomUnloadFailure = config.getString("roomUnloadFailure", this.roomUnloadFailure);
        this.pluginEnable = config.getString("pluginEnable", this.pluginEnable);
        this.pluginDisable = config.getString("pluginDisable", this.pluginDisable);
        this.useCmdInRoom = config.getString("useCmdInRoom", this.useCmdInRoom);
        this.cmdHelp = config.getString("cmdHelp", this.cmdHelp);
        this.userHelp = config.getString("userHelp", this.userHelp);
        this.noPermission = config.getString("noPermission", this.noPermission);
        this.joinRoom = config.getString("joinRoom", this.joinRoom);
        this.joinRoomOnRoom = config.getString("joinRoomOnRoom", this.joinRoomOnRoom);
        this.joinRoomOnRiding = config.getString("joinRoomOnRiding", this.joinRoomOnRiding);
        this.joinRandomRoom = config.getString("joinRandomRoom", this.joinRandomRoom);
        this.joinRoomIsPlaying = config.getString("joinRoomIsPlaying", this.joinRoomIsPlaying);
        this.joinRoomIsFull = config.getString("joinRoomIsFull", this.joinRoomIsFull);
        this.joinRoomIsNotFound = config.getString("joinRoomIsNotFound", this.joinRoomIsNotFound);
        this.joinRoomNotAvailable = config.getString("joinRoomNotAvailable", this.joinRoomNotAvailable);
        this.quitRoom = config.getString("quitRoom", this.quitRoom);
        this.quitRoomNotInRoom = config.getString("quitRoomNotInRoom", this.quitRoomNotInRoom);
        this.listRoom = config.getString("listRoom", this.listRoom);
        this.useCmdInCon = config.getString("useCmdInCon", this.useCmdInCon);
        this.adminHelp = config.getString("adminHelp", this.adminHelp);
        this.adminSetSpawn = config.getString("adminSetSpawn", this.adminSetSpawn);
        this.adminAddRandomSpawn = config.getString("adminAddRandomSpawn", this.adminAddRandomSpawn);
        this.adminNotNumber = config.getString("adminNotNumber", this.adminNotNumber);
        this.adminSetWaitTime = config.getString("adminSetWaitTime", this.adminSetWaitTime);
        this.adminSetGameTime = config.getString("adminSetGameTime", this.adminSetGameTime);
        this.adminSetGameTimeShort = config.getString("adminSetGameTimeShort", this.adminSetGameTimeShort);
        this.adminStartNoPlayer = config.getString("adminStartNoPlayer", this.adminStartNoPlayer);
        this.adminLevelNoRoom = config.getString("adminLevelNoRoom", this.adminLevelNoRoom);
        this.adminReload = config.getString("adminReload", this.adminReload);
        this.adminUnload = config.getString("adminUnload", this.adminUnload);
        this.roomSafeKick = config.getString("roomSafeKick", this.roomSafeKick);
        this.preyChat = config.getString("preyChat", this.preyChat);
        this.huntersChat = config.getString("huntersChat", this.huntersChat);
        this.tpJoinRoomLevel = config.getString("tpJoinRoomLevel", this.tpJoinRoomLevel);
        this.tpQuitRoomLevel = config.getString("tpQuitRoomLevel", this.tpQuitRoomLevel);
        this.itemQuitRoom = config.getString("itemQuitRoom", this.itemQuitRoom);
        this.itemQuitRoomLore = config.getString("itemQuitRoomLore", this.itemQuitRoomLore);
        this.Classic = config.getString("Classic", this.Classic);
        this.prey = config.getString("prey", this.prey);
        this.hunters = config.getString("hunters", this.hunters);
        this.death = config.getString("death", this.death);
        this.titlePreyTitle = config.getString("titlePreyTitle", this.titlePreyTitle);
        this.titlePreySubtitle = config.getString("titlePreySubtitle", this.titlePreySubtitle);
        this.titleHuntersTitle = config.getString("titleHuntersTitle", this.titleHuntersTitle);
        this.titleHuntersSubtitle = config.getString("titleHuntersSubtitle", this.titleHuntersSubtitle);
        this.titleVictoryHuntersTitle = config.getString("titleVictoryHuntersTitle", this.titleVictoryHuntersTitle);
        this.titleVictoryPreySubtitle = config.getString("titleVictoryPreySubtitle", this.titleVictoryPreySubtitle);
        this.victoryHuntersBottom = config.getString("victoryKillerBottom", this.victoryHuntersBottom);
        this.victoryHuntersScoreBoard = config.getString("victoryKillerScoreBoard", this.victoryHuntersScoreBoard);
        this.victoryPreyBottom = config.getString("victoryCommonPeopleBottom", this.victoryPreyBottom);
        this.victoryPreyScoreBoard = config.getString("victoryCommonPeopleScoreBoard", this.victoryPreyScoreBoard);
        this.scoreBoardTitle = config.getString("scoreBoardTitle", this.scoreBoardTitle);
        this.waitTimeScoreBoard = config.getString("waitTimeScoreBoard", this.waitTimeScoreBoard);
        this.waitScoreBoard = config.getString("waitScoreBoard", this.waitScoreBoard);
        this.waitTimeBottom = config.getString("waitTimeBottom", this.waitTimeBottom);
        this.waitBottom = config.getString("waitBottom", this.waitBottom);
        this.gameTimeScoreBoard = config.getString("gameTimeScoreBoard", this.gameTimeScoreBoard);
        this.gameTimeBottom = config.getString("gameTimeBottom", this.gameTimeBottom);
        this.huntersKillPrey = config.getString("huntersKillPrey", this.huntersKillPrey);
        this.userMenuButton1 = config.getString("userMenuButton1", this.userMenuButton1);
        this.userMenuButton2 = config.getString("userMenuButton2", this.userMenuButton2);
        this.userMenuButton3 = config.getString("userMenuButton3", this.userMenuButton3);
        this.adminMenuSetLevel = config.getString("adminMenuSetLevel", this.adminMenuSetLevel);
        this.adminMenuButton1 = config.getString("adminMenuButton1", this.adminMenuButton1);
        this.adminMenuButton2 = config.getString("adminMenuButton2", this.adminMenuButton2);
        this.adminMenuButton3 = config.getString("adminMenuButton3", this.adminMenuButton3);
        this.adminMenuButton4 = config.getString("adminMenuButton4", this.adminMenuButton4);
        this.adminMenuButton5 = config.getString("adminMenuButton5", this.adminMenuButton5);
        this.adminTimeMenuInputText1 = config.getString("adminTimeMenuInputText1", this.adminTimeMenuInputText1);
        this.adminTimeMenuInputText2 = config.getString("adminTimeMenuInputText2", this.adminTimeMenuInputText2);
        this.joinRoomOK = config.getString("joinRoomOK", this.joinRoomOK);
        this.buttonOK = config.getString("buttonOK", this.buttonOK);
        this.buttonReturn = config.getString("buttonReturn", this.buttonReturn);
    }

}
