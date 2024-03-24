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
import java.util.Random;

@SpringBootTest
class SandboxServiceApplicationTests {

    @Autowired
    Sandbox sandbox;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Test
    void testJudgeQueue() {
        JudgeTask judgeTask = null;

        // String s = inputStreamToString(inputStream);
        judgeTask = new JudgeTask(1L, new String[]{"1 2", "3 4", "4 -1"}, "import java.util.Scanner;\n" + "\n" + "public class Main {\n" + "    public static void main(String[] args) {\n" + "        Scanner in=new Scanner(System.in);\n" + "        int a=in.nextInt(),b=in.nextInt();\n" + "        System.out.println(a+b);\n" + "    }\n" + "}\n", Language.JAVA8, new ResourceLimit(1000, 512), null);

        rabbitTemplate.convertAndSend("judge.queue", judgeTask);
    }

    @Test
    void noInputsTest() {
        JudgeTask judgeTask = null;

        // String s = inputStreamToString(inputStream);
        judgeTask = new JudgeTask(2L, new String[]{""}, "public class Main {\n" + "    public static void main(String[] args) throws InterruptedException {\n" + "         Thread.sleep(2000L);\n" + "        System.out.println(\"yes\");\n" + "    }\n" + "}\n", Language.JAVA8, new ResourceLimit(1000, 512), null);

        rabbitTemplate.convertAndSend("judge.queue", judgeTask);
    }

    @Test
    void runtimeErrorTest() {
        JudgeTask judgeTask = null;
        long id = new Random().nextLong() % 1000;

        // String s = inputStreamToString(inputStream);
        judgeTask = new JudgeTask(id, new String[]{""}, "public class Main {\n" + "    public static void main(String[] args) throws InterruptedException {\n" + "        int[] a=new int[10];\n" + "        System.out.println(a[11]);\n" + "    }\n" + "}", Language.JAVA8, new ResourceLimit(1000, 512), null);

        rabbitTemplate.convertAndSend("judge.queue", judgeTask);
    }

    @Test
    void mleTest() {

        JudgeTask judgeTask = null;
        long id = new Random().nextLong() % 1000;

        // String s = inputStreamToString(inputStream);
        judgeTask = new JudgeTask(id, new String[]{""}, "public class Main {\n" + "            public static void main(String[] args) throws InterruptedException {\n" + "                int[] a=new int[1000000000];\n" + "                System.out.println(a[1000000000-10]);\n" + "            }\n" + "        }", Language.JAVA8, new ResourceLimit(1000, 512), null);

        rabbitTemplate.convertAndSend("judge.queue", judgeTask);
    }

    @Test
    void outPutsFormat() {
        long id = new Random().nextLong() % 1000;

        JudgeTask judgeTask = null;
        judgeTask = new JudgeTask(id, new String[]{""}, "public class Main {\n" + "    public static void main(String[] args) throws InterruptedException {\n" + "        System.out.println(\"12 21\");System.out.println(\"12 23\");\n" + "    }\n" + "}\n", Language.JAVA8, new ResourceLimit(1000, 512), null);
        rabbitTemplate.convertAndSend("judge.queue", judgeTask);

    }

    // 过河卒测试
    @Test
    void stdTest() {
        JudgeTask judgeTask = null;
        long id = (new Random().nextLong() % 1000 + 1000) % 1000;
        judgeTask = new JudgeTask(id, new String[]{"3 3 2 3", "4 8 2 4", "20 20 4 0"},"import java.util.LinkedList;\n" + "import java.util.Queue;\n" + "import java.util.Scanner;\n" + "\n" + "public class Main {\n" + "\n" + "    static class Position {\n" + "        int x, y;\n" + "\n" + "        Position(int x, int y) {\n" + "            this.x = x;\n" + "            this.y = y;\n" + "        }\n" + "    }\n" + "\n" + "    static int[][] direction = {{1, 0}, {0, 1}};\n" + "    static int[][] horseDir = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};\n" + "    static boolean[][] visited;\n" + "\n" + "    public static int BFS(int startX, int startY, int endX, int endY) {\n" + "        int count = 0;\n" + "        Queue<Position> queue = new LinkedList<>();\n" + "        queue.add(new Position(startX, startY));\n" + "\n" + "        while (!queue.isEmpty()) {\n" + "            Position current = queue.poll();\n" + "\n" + "            for (int i = 0; i < 2; i++) {\n" + "                Position next = new Position(current.x + direction[i][0], current.y + direction[i][1]);\n" + "\n" + "                if (next.x >= 0 && next.x <= endX && next.y >= 0 && next.y <= endY && !visited[next.x][next.y]) {\n" + "                    if (next.x == endX && next.y == endY) {\n" + "                        count++;\n" + "                    }\n" + "                    queue.add(next);\n" + "                }\n" + "            }\n" + "        }\n" + "\n" + "        return count;\n" + "    }\n" + "\n" + "    public static void main(String[] args) {\n" + "        Scanner scanner = new Scanner(System.in);\n" + "        int endX = scanner.nextInt();\n" + "        int endY = scanner.nextInt();\n" + "        int horseX = scanner.nextInt();\n" + "        int horseY = scanner.nextInt();\n" + "\n" + "        visited = new boolean[endX + 1][endY + 1];\n" + "\n" + "        visited[horseX][horseY] = true;\n" + "        for (int i = 0; i < horseDir.length; i++) {\n" + "            int x = horseX + horseDir[i][0];\n" + "            int y = horseY + horseDir[i][1];\n" + "            if (x >= 0 && x <= endX && y >= 0 && y <= endY) {\n" + "                visited[x][y] = true;\n" + "            }\n" + "        }\n" + "\n" + "        System.out.println(BFS(0, 0, endX, endY));\n" + "    }\n" + "}\n",Language.JAVA8,new ResourceLimit(1000,128),null);
        rabbitTemplate.convertAndSend("judge.queue", judgeTask);
    }

    /*-----------------------------*/

    @Test
    void contextLoads() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder().dockerHost(config.getDockerHost()).sslConfig(config.getSSLConfig()).maxConnections(100).connectionTimeout(Duration.ofSeconds(30)).responseTimeout(Duration.ofSeconds(45)).build();

        DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
        JudgeTask judgeTask = null;

        String code="import java.util.LinkedList;\n" + "import java.util.Queue;\n" + "import java.util.Scanner;\n" + "\n" + "public class Main {\n" + "\n" + "    static class Position {\n" + "        int x, y;\n" + "\n" + "        Position(int x, int y) {\n" + "            this.x = x;\n" + "            this.y = y;\n" + "        }\n" + "    }\n" + "\n" + "    static int[][] direction = {{1, 0}, {0, 1}};\n" + "    static int[][] horseDir = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};\n" + "    static boolean[][] visited;\n" + "\n" + "    public static int BFS(int startX, int startY, int endX, int endY) {\n" + "        int count = 0;\n" + "        Queue<Position> queue = new LinkedList<>();\n" + "        queue.add(new Position(startX, startY));\n" + "\n" + "        while (!queue.isEmpty()) {\n" + "            Position current = queue.poll();\n" + "\n" + "            for (int i = 0; i < 2; i++) {\n" + "                Position next = new Position(current.x + direction[i][0], current.y + direction[i][1]);\n" + "\n" + "                if (next.x >= 0 && next.x <= endX && next.y >= 0 && next.y <= endY && !visited[next.x][next.y]) {\n" + "                    if (next.x == endX && next.y == endY) {\n" + "                        count++;\n" + "                    }\n" + "                    queue.add(next);\n" + "                }\n" + "            }\n" + "        }\n" + "\n" + "        return count;\n" + "    }\n" + "\n" + "    public static void main(String[] args) {\n" + "        try {\n" + "            Thread.sleep(2000);\n" + "        } catch (InterruptedException e) {\n" + "            throw new RuntimeException(e);\n" + "        }\n" + "        Scanner scanner = new Scanner(System.in);\n" + "        int endX = scanner.nextInt();\n" + "        int endY = scanner.nextInt();\n" + "        int horseX = scanner.nextInt();\n" + "        int horseY = scanner.nextInt();\n" + "\n" + "        visited = new boolean[endX + 1][endY + 1];\n" + "\n" + "        visited[horseX][horseY] = true;\n" + "        for (int i = 0; i < horseDir.length; i++) {\n" + "            int x = horseX + horseDir[i][0];\n" + "            int y = horseY + horseDir[i][1];\n" + "            if (x >= 0 && x <= endX && y >= 0 && y <= endY) {\n" + "                visited[x][y] = true;\n" + "            }\n" + "        }\n" + "\n" + "        System.out.println(BFS(0, 0, endX, endY));\n" + "    }\n" + "}\n";
        // String s = inputStreamToString(inputStream);
        judgeTask = new JudgeTask(1L, new String[]{"1 2", "3 4", "4 -1"}, code, Language.JAVA8, new ResourceLimit(1000, 512), null);

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
