import com.mengqifeng.www.worker.IWorker;
import com.mengqifeng.www.worker.ShuffleWorker;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        long start = System.nanoTime();
        // 1. 获得环境参数:
        // 2. 读取输入的文件路径配置、参数:
        String tmpDir = "D:/work/tmp";
        String outDir = "D:/work/out";
        String inFile1 = "D:/work/old/0604全量包/0604全量包.txt";
        String inFile2 = "D:/work/old/0604全量包/0604全量包.txt";
        // 3. 采样文件n个块,生成采样结果:
        // 4. 根据上述结果,选择执行计划:
        // 5. 执行选择的方法:
        IWorker worker = new ShuffleWorker(tmpDir, outDir, inFile1, inFile2);
        worker.run();
        // 6. clear资源:
        long end = System.nanoTime();
        System.out.println("运行时间: " + (double) (end - start) / 1000000000 + "s");
    }
}
