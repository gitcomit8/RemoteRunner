package com.mirza.remoterunner;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;

public class RemoteRunner {
    public static String executeCommand(String host, int port, String user, String password, String command) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            session.setPassword(password);

            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            java.io.InputStream in = channel.getInputStream();

            channel.connect();
            byte[] tmp = new byte[1024];
            StringBuilder output = new StringBuilder();
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0) break;
                    output.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    if (in.available() > 0) continue;
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            }
            channel.disconnect();
            session.disconnect();
            return output.toString();
        } catch (JSchException | IOException e) {
            return "Error: " + e.getMessage();
        }
    }
}