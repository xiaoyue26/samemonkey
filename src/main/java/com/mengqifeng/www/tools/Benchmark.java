package com.mengqifeng.www.tools;




public class Benchmark {

    private void test() {
    }

    public static void main(String[] args) {
        /**
         *  测试配置:
         *  1. 样本区别: 10G相同、10G不同、10G 10%~90%  相同;
         *  2. splitSize区别: 64K ~ 1MB ~ 16MB ~ 128MB
         *  3. 是否使用bloomfilter: 是、否
         * */
        // 相同: gen1 copy to 2
        // 不同: gen1 gen2  50000000 lines
        /**
         * gen1: 10%
         * gen2: 10%
         * copy1=>2
         * gen1: 90%
         * gen2: 80%
         * */









    }
}
