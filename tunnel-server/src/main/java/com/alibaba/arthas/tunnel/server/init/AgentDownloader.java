package com.alibaba.arthas.tunnel.server.init;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class AgentDownloader implements CommandLineRunner {

    @Value("${agent.tar.url}")
    private String agentUrl;

    @Value("${agent.tar.path}")
    private String agentPath;

    @Override
    public void run(String... args) throws Exception {

        // 下载文件
        downloadFile(agentUrl, agentPath);

        System.out.println("File downloaded successfully to: " + agentPath);
    }

    private void downloadFile(String fileUrl, String localFilePath) throws IOException {
        // 使用 Apache HttpClient 执行 HTTP GET 请求
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(fileUrl);
        HttpResponse response = httpClient.execute(httpGet);
        // 获取响应的输入流
        try (InputStream inputStream = response.getEntity().getContent()) {
            Files.copy(inputStream, Paths.get(localFilePath), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
