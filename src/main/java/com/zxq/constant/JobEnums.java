package com.zxq.constant;

/**
 * job 相关枚举值
 * @author zxq
 * @date 2020/3/24 17:04
 **/
public class JobEnums {

    public enum JobStatus {

        /**
         * 已暂停
         */
        PAUSING(0, "已暂停"),
        /**
         * 运行中
         */
        RUNNING(1, "运行中"),
        /**
         * 已删除
         */
        DELETED(-1, "已删除");

        /**
         * 状态
         */
        private Integer status;

        /**
         * 状态描述
         */
        private String value;

        JobStatus(Integer status, String value) {
            this.status = status;
            this.value = value;
        }

        public Integer status() {
            return this.status;
        }

        public String value() {
            return this.value;
        }


    }

    public enum JobLogStatus {

        /**
         * 已暂停
         */
        FAILURE(0, "执行失败"),
        /**
         * 运行中
         */
        SUCCESS(1, "执行成功");

        /**
         * 状态
         */
        private Integer status;

        /**
         * 状态描述
         */
        private String value;

        JobLogStatus(Integer status, String value) {
            this.status = status;
            this.value = value;
        }

        public Integer status() {
            return this.status;
        }

        public String value() {
            return this.value;
        }


    }

}
