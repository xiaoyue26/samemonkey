import com.mengqifeng.www.logic.WorkerParam;
import com.mengqifeng.www.utils.Logger;
import com.mengqifeng.www.worker.IWorker;
import com.mengqifeng.www.worker.ShuffleWorker;

import java.io.IOException;

public class Main {
    private static final Logger logger = new Logger();

    public static void main(String[] args) throws IOException {
        long start = System.nanoTime();
        // 1. 获得环境参数:
        // 2. 读取输入的文件路径配置、参数:
        WorkerParam param = WorkerParam.parseArgs(args);
        // 3. 采样文件n个块,生成采样结果:
        // 4. 根据上述结果,选择执行计划:
        // 5. 执行选择的方法:
        IWorker worker = new ShuffleWorker(param);
        worker.run();
        // 6. clear资源:
        long end = System.nanoTime();
        logger.info("运行时间: %fs", (double) (end - start) / 1000000000);
        // 运行时间: 2769.734857601s
    }
}
