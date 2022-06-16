package com.seagame.ext.services;

import com.creants.creants_2x.QAntServer;
import com.creants.creants_2x.core.entities.Zone;
import com.seagame.ext.util.SourceFileHelper;
import lombok.Getter;
import lombok.ToString;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author LamHM
 */
//@RestController
//@RequestMapping("/game-setting")
public class SettingController {
    private static final List<String> fileList;

    static {
        fileList = new ArrayList<>();
        File folder = new File("resources");
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (File listOfFile : listOfFiles) {
                if (listOfFile.isFile()) {
                    fileList.add(listOfFile.getName());
                }
            }
        }
    }


    @RequestMapping(path = "/file/view/{fileName}")
    public @ResponseBody
    FileSystemResource viewFile(@PathVariable String fileName) {
        return new FileSystemResource(new File("resources/" + validateFileName(fileName)));
    }


    @PostMapping(path = "/file/update", consumes = MediaType.APPLICATION_XML_VALUE)
    public @ResponseBody
    FileSystemResource updateFile(@RequestParam(value = "fileName") String fileName,
                                  @RequestParam(value = "content") String content) {
        try {
            SourceFileHelper.updateFile(content, fileName);
            return new FileSystemResource(new File("resources/" + validateFileName(fileName)));
        } catch (IOException e) {
            return null;
        }
    }

    @RequestMapping(path = "/file/list", produces = "application/json; charset=UTF-8")
    public @ResponseBody
    List<String> reloadFile() {
        return fileList;
    }

    @GetMapping(path = "/serverList", produces = "application/json; charset=UTF-8")
    public @ResponseBody
    List<Server> serverList() {
        List<Server> collect = QAntServer.getInstance().getZoneManager().getZoneList().stream().filter(zone -> zone.getId() != -1).map(this::buildServer).collect(Collectors.toList());
        collect.stream().max(Comparator.comparingInt(Server::getServerNo)).ifPresent(server -> server.parseStatus(true));
        return collect;
    }

    private Server buildServer(Zone zone) {
        Server server = new Server(zone);
        server.parseStatus(false);
        return server;
    }

    private String validateFileName(String reqFileName) {
        return reqFileName.replace("@", ".");
    }

}

@Getter
@ToString
class Server {
    private static final int CLOSED = -2;
    private static final int FULL = -1;
    private static final int NORMAL = 1;
    private static final int GOOD = 2;
    private static final int EXCELLENT = 3;
    private static final int NEW = 4;
    String id;
    String name;
    int userCount;
    int maxUser;
    int status;
    String serverCode;
    int serverNo;
    long serverTime;

    public Server(Zone zone) {
        this.id = zone.getName();
        this.name = parseName(zone.getName());
        this.userCount = zone.getUserCount();
        this.maxUser = zone.getMaxAllowedUsers();
        this.status = NEW;
        serverTime = System.currentTimeMillis();
    }


    private String parseName(String serverId) {
        String serverCode = this.serverCode = serverId.substring(0, serverId.length() - 1);
        int no = this.serverNo = Integer.parseInt(serverId.substring(serverId.length() - 1));
        String name = "";
        switch (serverCode) {
            case "os":
                name += "Huyền Thoại ";
                break;
            case "mk":
                name += "Huyền Thoại ";
                break;
            case "nf":
                name += "Crypto ";
                break;
            default:
                name += "Huyền Thoại ";
                break;
        }
        return name + no;

    }

    void parseStatus(boolean isNew) {
        float percent = 1f * this.userCount / this.maxUser;
        if (isNew) {
            this.status = NEW;
        } else if (percent > 0.95f) {
            this.status = FULL;
        } else if (percent > 0.50f) {
            this.status = NORMAL;
        } else if (percent > 0.25f) {
            this.status = GOOD;
        } else if (percent >= 0f) {
            this.status = EXCELLENT;
        } else {
            this.status = CLOSED;
        }
    }

}