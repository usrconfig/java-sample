package com.seagame.ext.bot;

import com.creants.creants_2x.core.util.AppConfig;
import com.creants.creants_2x.core.util.QAntTracer;
import com.seagame.ext.entities.Player;
import com.seagame.ext.managers.AbstractExtensionManager;
import com.seagame.ext.util.UserHelper;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Getter
@Service
public class UnityBotManager extends AbstractExtensionManager {
    public static final String BOT_PREFIX = "unityBot#";
    private String unityExe;
    private String workingDir;
    private Map<String, BotInfo> mapBot;
    private Map<String, BotInfo> mapIDBot;
    private AtomicInteger atomicInteger;
    private ArrayList<BotInfo> hosts;

    private static AtomicLong time = new AtomicLong(1);

    public UnityBotManager() {
        mapBot = new HashMap<>();
        mapIDBot = new HashMap<>();
        atomicInteger = new AtomicInteger();
        this.hosts = new ArrayList<>();
    }

    private List<Thread> launcherThreads;

    public static boolean isHost(String deviceId) {
        return (deviceId != null && deviceId.contains(BOT_PREFIX));
    }

    public BotInfo createBot(String key) {
        return createBot(key, this.extension.getParentZone().getName());
    }

    public BotInfo createBot(String key, String zoneName) {
        String botID = UserHelper.buildHexName(key, time.getAndIncrement());
        BotInfo botInfo = new BotInfo(botID, botID, zoneName);
        botInfo.setGroup(key);
        return botInfo;
    }


    public void trackBotIn(Player player) {
        String deviceId = player.getDeviceId();
        if (isHost(deviceId)) {
            BotInfo botInfo;
            botInfo = mapBot.getOrDefault(pickBotDeviceID(deviceId), new BotInfo());
            botInfo.active(player);
            //ghi lại id để track out
            mapIDBot.put(botInfo.getUserId(), botInfo);
            QAntTracer.info(this.getClass(), "Unity process Bot In: " + botInfo.toString());
            QAntTracer.info(this.getClass(), "Unity process Bot In DeviceID: " + deviceId);
        }
    }

    private String pickBotDeviceID(String deviceId) {
        String[] idPart = deviceId.split("##");
        if (idPart.length == 1) {
            return idPart[0];
        } else {
            return idPart[1];
        }
    }

    public void trackBotOut(String userId) {
        if (userId != null && mapIDBot.containsKey(userId)) {
            BotInfo botInfo = mapIDBot.get(userId);
            botInfo.inactive();
            QAntTracer.info(this.getClass(), "Unity process Bot Out, trying....: " + botInfo.toString());
        }
    }

    public class UnityRunner implements Runnable {
        private String token;
        private String zone;
        private int room;
        private String session;
        private String mapKey;


        public UnityRunner(BotInfo s) {
            this.session = s.getBotToken();
            this.token = s.buildBotString();
            this.zone = s.getZone();
            this.room = s.getRoom();
            this.mapKey = s.getGroup();
        }

        @Override
        public void run() {
            try {
                String cmd = workingDir + "/" + unityExe;
                ProcessBuilder processBuilder = new ProcessBuilder(cmd, "-batchmode", "-logFile", workingDir
                        + "/unity_" + this.session + ".log", "--sessionID", this.session, "--token", this.token, "--zone", this.zone, "--room", String.valueOf(this.room), "--classZone", this.mapKey, "-Xms128m", "-Xmx256m");
                processBuilder.directory(new File(workingDir));
                QAntTracer.info(this.getClass(), "Unity Bot__" + String.join("_", processBuilder.command()));
                Process proc = processBuilder.start();
                try {
                    long pidOfProcess = getPidOfProcess(proc);
                    QAntTracer.info(this.getClass(), "Unity process : " + pidOfProcess + "__" + String.join("_", processBuilder.command()));
                } catch (Exception ignored) {
                }
                int exit = proc.waitFor();
                QAntTracer.info(this.getClass(), "Unity process terminated with code: " + exit);
            } catch (IOException ex) {
                QAntTracer.info(this.getClass(), "Error launching Unity executable: " + ex);
                ex.printStackTrace();
            } catch (Exception ex) {
                QAntTracer.info(this.getClass(), "Unexpected exceptions: " + ex);
            }
        }
    }

    public static synchronized long getPidOfProcess(Process p) {
        long pid = -1;
        try {
            if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
                Field f = p.getClass().getDeclaredField("pid");
                f.setAccessible(true);
                pid = f.getLong(p);
                f.setAccessible(false);
            }
        } catch (Exception e) {
            pid = -1;
        }
        return pid;
    }

    public void initBaseRoom() {
        unityExe = AppConfig.getProps().getProperty("unity.exe", "MKLinux.x86_64");
        String unityDir = AppConfig.getProps().getProperty("unity.dir", "unity/");
        String zoneName = this.extension.getParentZone().getName();
        this.workingDir = new File(unityDir).getAbsolutePath();

        QAntTracer.info(this.getClass(), "========================= Start Default Unity BOT ========================= " + zoneName);
        this.launcherThreads = new ArrayList<>();
//        List<String> hosts = Collections.singletonList("defaultSSID" + this.extension.getParentZone().getName());
//        GameConfig.getInstance().getMapSet().stream().filter(s -> !s.equals("MAP900")).forEach(s -> IntStream.range(1, 2).forEach(value -> {
//        BotInfo botInfoTown = createBot("Room1");
//        hosts.add(botInfoTown);
//        try {
//            this.startNewBot(botInfoTown);
//        } catch (Exception e) {
//            QAntTracer.info(this.getClass(), e.toString());
//            e.printStackTrace();
//        }
//        }));
        QAntTracer.info(this.getClass(), "========================= Start Bot Complete =========================");
        QAntTracer.info(this.getClass(), hosts.toString());

    }

    public BotInfo findBotInfo(String port, String map, String zone) {
//        PortalBase portalBase = GameConfig.getInstance().getPortalBaseMap().get(port + NetworkConstant.SEPERATE_OTHER_ITEM + map);
//        if (portalBase == null)
//            return null;
//        return findBotInfo(portalBase.getGoToMapID(), zone);

        return null;
    }

    public BotInfo findBotInfo(String group, String zone) {
        QAntTracer.debug(UnityBotManager.class, "findBotInfo : " + hosts.toString());
        Collections.shuffle(hosts);
        Optional<BotInfo> min = hosts.stream().filter(botInfo -> (botInfo.getGroup().equals(group) && botInfo.getZone().equals(zone) && testActiveBot(botInfo))).findFirst();
        return min.orElse(null);
    }

    public BotInfo findBotBossInfo(String group, String zone) {
        QAntTracer.debug(UnityBotManager.class, "findBotBossInfo : " + hosts.toString());
        Optional<BotInfo> min = hosts.stream().sorted(Comparator.comparingInt(BotInfo::getPriority)).filter(botInfo -> (botInfo.getGroup().equals(group) && botInfo.getZone().equals(zone) && testActiveBot(botInfo))).findFirst();
        return min.orElse(null);
    }

    private boolean testActiveBot(BotInfo botInfo) {
        if (botInfo.isActive()) {
            return true;
        }
        //TODO skip restart bot on Dis
//        this.startNewBot(botInfo);
        return false;
    }

    public void startNewBot(BotInfo s) {
        if (s.isActive())
            return;
        s.setRoom(atomicInteger.getAndIncrement());
        mapBot.put(s.buildBotString(), s);
        Thread launcherThread = new Thread(new UnityRunner(s), "unity-worker:" + s);
        launcherThread.start();
        this.launcherThreads.add(launcherThread);
        QAntTracer.info(this.getClass(), "UnityLauncher started : " + s);
    }
}
