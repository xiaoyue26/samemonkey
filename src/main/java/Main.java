import com.mengqifeng.www.logic.ConsoleParam;
import com.mengqifeng.www.utils.FileUtils;
import com.mengqifeng.www.utils.LogFactory;
import com.mengqifeng.www.utils.Logger;
import com.mengqifeng.www.worker.IWorker;
import com.mengqifeng.www.worker.WorkerFactory;

import java.io.File;
import java.io.IOException;

public class Main {
    private static final Logger logger = LogFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        // FileUtils.deleteDirectory(new File("D:/work/out"));
        long start = System.nanoTime();
        // 1. 获得环境参数:
        // 2. 读取输入的文件路径配置、参数:
        ConsoleParam param = ConsoleParam.parseArgs(args);
        // 3. 采样文件n个块,生成采样结果:
        // 4. 根据上述结果,选择执行计划:
        // 5. 执行选择的方法:
        IWorker worker = WorkerFactory.createShuffleWorker(param);
        worker.run();
        // 6. clear资源:
        long end = System.nanoTime();
        logger.info("运行时间: %fs", (double) (end - start) / 1000000000);

    }
}
