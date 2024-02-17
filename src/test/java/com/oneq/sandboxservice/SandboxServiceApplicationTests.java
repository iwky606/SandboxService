package com.oneq.sandboxservice;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.oneq.sandboxservice.model.dto.JudgeTask;
import com.oneq.sandboxservice.model.dto.ResourceLimit;
import com.oneq.sandboxservice.model.enums.Language;
import com.oneq.sandboxservice.service.Sandbox;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@SpringBootTest
class SandboxServiceApplicationTests {

    @Autowired
    Sandbox sandbox;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    void testJudgeQueue(){
        JudgeTask judgeTask = null;

        // String s = inputStreamToString(inputStream);
        judgeTask = new JudgeTask(1L, new String[]{"1 2","3 4","4 -1"}, "import java.util.Scanner;\n" + "\n" + "public class Main {\n" + "    public static void main(String[] args) {\n" + "        Scanner in=new Scanner(System.in);\n" + "        int a=in.nextInt(),b=in.nextInt();\n" + "        System.out.println(a+b);\n" + "    }\n" + "}\n", Language.JAVA8, new ResourceLimit(1000, 512), null);

        rabbitTemplate.convertAndSend("judge.queue",judgeTask);
    }

    /*-----------------------------*/

    @Test
    void contextLoads() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder().dockerHost(config.getDockerHost()).sslConfig(config.getSSLConfig()).maxConnections(100).connectionTimeout(Duration.ofSeconds(30)).responseTimeout(Duration.ofSeconds(45)).build();

        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        JudgeTask judgeTask = null;

        // String s = inputStreamToString(inputStream);
        judgeTask = new JudgeTask(1L, new String[]{"1 2","3 4","4 -1"}, "import java.util.Scanner;\n" + "\n" + "public class Main {\n" + "    public static void main(String[] args) {\n" + "        Scanner in=new Scanner(System.in);\n" + "        int a=in.nextInt(),b=in.nextInt();\n" + "        System.out.println(a+b);\n" + "    }\n" + "}\n", Language.JAVA8, new ResourceLimit(1000, 512), null);

        sandbox.doTask(judgeTask, dockerClient);
    }

    public String inputStreamToString(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }

    @Test
    void test() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        System.out.println(config.getDockerHost());
        System.out.println(config.getSSLConfig());
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder().dockerHost(config.getDockerHost()).sslConfig(config.getSSLConfig()).maxConnections(100).connectionTimeout(Duration.ofSeconds(30)).responseTimeout(Duration.ofSeconds(45)).build();

        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);

        long startTime = System.currentTimeMillis();
        // 定义卷挂载：宿主机路径映射到容器内的路径
        Volume volume = new Volume("/app");
        Bind bind = new Bind("/home/oneq/code/Java/Test/src/main/resources", volume);

        // 创建容器
        String imageId = "d2c94e258dcb";

        CreateContainerResponse container = dockerClient.createContainerCmd(imageId).withVolumes(volume).withBinds(bind).withTty(true) // 对应 `-t` 选项
                .withStdinOpen(true) // 对应 `-i` 选项
                .exec();

        // 获取容器 ID
        String containerId = container.getId();

        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();
        long endTime = System.currentTimeMillis();

        System.out.println("容器已启动，容器ID: " + containerId);
        System.out.println("创建容器耗时：" + (endTime - startTime));
    }

}
