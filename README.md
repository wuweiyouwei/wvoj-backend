## 项目简介

在线代码驿站（OJ = Online Judge 在线判题评测系统），核心功能：用户可以选择题目，在线做题，编写代码并提交代码；系统会根据用户提交的代码，根据我们出题人设置的答案，来判断用户的提交结果是否正确。

## 技术栈

+ Spring Boot
+ MySQL
+ Redis
+ Mybatis-plus
+ RabbitMQ
+ Docker

## 核心业务流程

![image-20240316140723639](https://typora-1316924729.cos.ap-beijing.myqcloud.com/PicGoimage-20240316140723639.png)

![image-20240316140744400](https://typora-1316924729.cos.ap-beijing.myqcloud.com/PicGoimage-20240316140744400.png)

## 核心功能

1）权限校验
谁能提交代码，谁不能提交代码
2）代码沙箱（安全沙箱）
用户代码藏毒：写木马程序、修改系统权限
沙箱：隔离的、安全的环境，用户提交的代码不会影响到沙箱以外的系统运行
3）判题功能
题目用例的比对，结果的验证
4）任务调度
服务器资源有限，用户要排队，按照题目提交顺序执行判题。而不是直接拒绝

## 系统功能梳理

1. 用户模块

1. 1. 注册
   2. 登录

1. 题目模块

1. 1. 创建题目（admin）
   2. 删除题目（admin）
   3. 修改题目（admin）
   4. 搜索题目（user）
   5. 在线做题（题目详情页）

1. 判题模块

1. 1. 提交判题（结果是否正确）
   2. 错误处理（内存溢出、安全性、超时）
   3. 代码沙箱（自主实现）
   4. 开放接口（提供一个独立的新服务）

## 库表设计

用户表、题目表、题目提交表、消息发送日志表

`

```sql
# 建表脚本
# wv

drop database if exists db_oj;
-- 创建库
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    unionId      varchar(256)                           null comment '微信开放平台id',
    mpOpenId     varchar(256)                           null comment '公众号openId',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    accessKey    varchar(512)                           not null comment 'accessKey',
    secretKey    varchar(512)                           not null comment 'secretKey',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_unionId (unionId)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 切换库
create database if not exists db_oj;

-- 用户表
use db_oj;

-- 题目表
create table if not exists question
(
    id          bigint auto_increment comment 'id' primary key,
    title       varchar(512)                       null comment '标题',
    content     text                               null comment '内容',
    tags        varchar(1024)                      null comment '标签列表（json 数组）',
    answer      text                               null comment '题目答案',
    submitNum   int      default 0                 not null comment '题目提交数',
    acceptedNum int      default 0                 not null comment '题目通过数',
    judgeCase   text                               null comment '判题用例（json 数组）',
    judgeConfig text                               null comment '判题配置（json 对象）',
    thumbNum    int      default 0                 not null comment '点赞数',
    favourNum   int      default 0                 not null comment '收藏数',
    userId      bigint                             not null comment '创建用户 id',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete    tinyint  default 0                 not null comment '是否删除',
    index idx_userId (userId)
) comment '题目' collate = utf8mb4_unicode_ci;

-- 题目提交表
create table if not exists question_submit
(
    id         bigint auto_increment comment 'id' primary key,
    language   varchar(128)                       not null comment '编程语言',
    code       text                               not null comment '用户代码',
    judgeInfo  text                               null comment '判题信息（json 对象）',
    status     int      default 0                 not null comment '判题状态（0 - 待判题、1 - 判题中、2 - 成功、3 - 失败）',
    questionId bigint                             not null comment '题目 id',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    index idx_questionId (questionId),
    index idx_userId (userId)
) comment '题目提交';

-- 消息发送日志表
create table message_send_log
(
    msgId            bigint auto_increment comment 'id' primary key
                                                        not null comment '消息id（uuid）',
    questionSubmitId bigint                             null comment '题目提交id',
    routeKey         varchar(255)                       null comment '队列名字',
    `status`         tinyint  default 0                 null comment '0-发送中 1-发送成功 2-发送失败',
    `exchange`       varchar(255)                       null comment '交换机名字',
    tryCount         tinyint                            null comment '重试次数',
    tryTime          datetime                           null comment '第一次重试时间',
    createTime       datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime       datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '修改时间',
    isDelete         tinyint  default 0                 null comment '是否删除'
)
    comment '消息发送日志表';
```

`

## 代码沙箱实现

**何为代码沙箱？**

**只负责接收代码和输入，返回编译运行的结果，不负责判题（可以做作为独立的服务 / 项目，提供给其他需要执行代码的项目使用）**



### Java 原生实现代码沙箱

核心思路：Java 源代码 => 编译（javac）=> 运行（java）

核心依赖：Java 进程类 Process

业务逻辑：

1. 把用户的代码保存为文件
2. 编译代码，得到 class 文件
3. 执行代码，得到输出结果
4. 收集整理输出结果
5. 文件清理，释放空间
6. 错误处理，提升程序健壮性



#### 安全性分析

用户提交恶意代码，危害系统

1. 执行阻塞（耗用时间）
2. 内存占用（耗用内存）
3. 读取文件（信息泄露）
4. 修改文件（写入木马）
5. 执行其他操作（高危操作）



#### 如何保证沙箱安全

1）超时控制：通过创建一个守护线程，超时后自动中断 Process 实现

```java
 new Thread(() -> {
     try {
         Thread.sleep(TIME_OUT);
         System.out.println("超时了，中断");
         runProcess.destroy();
     } catch (InterruptedException e) {
         throw new RuntimeException(e);
     }
 }).start();
```

2）限制给用户分配的资源

我们不能让每个 java 进程的执行占用的 JVM 最大堆内存空间都和系统默认的一致（我的 JVM 默认最大占用 8G 内存），实际上应该更小（执行用户的题目代码也不需要这么多），比如说 256MB。
在启动 Java 程序时，可以指定 JVM 的参数：-Xmx256m（最大堆空间大小）



3）代码限制（黑白名单）

先定义一个黑名单
可以使用 Hutool 的工具类 WordTree 来检验字符串中是否包含黑名单字段

```java
    /**
     * 黑名单字段方式，防止用户恶意操作
     */
    private static final List<String> BLACK_LIST = Arrays.asList("exec", "Files");


    public static final WordTree WORD_TREE;

    static {
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(BLACK_LIST);
    }

	FoundWord foundWord = WORD_TREE.matchWord(code);
        if (foundWord != null) {
            System.out.println("发现违禁词：" + foundWord.getWord());
            return null;
        }

```



4）限制用户的操作权限（文件、网络、指令）

限制用户对文件、内存、CPU、网络等资源的访问和操作。

通过自定义 Java 安全管理器（实现 Security Manager）重写 checkWrite()、checkExec() 限制 Java 代码的执行权限。



5）运行环境隔离

Docker：系统层面上，把用户程序封装到沙箱里，和宿主机（我们的电脑/服务器）隔离开。





### Docker 代码沙箱

为什么要用 Docker 技术？

**为了提升系统的安全性，把不同程序和宿主机进行隔离，使得某个程序和应用的执行不会影响到系统本身。**

什么是容器？

**理解为对一系列应用程序、服务、和环境的封装，从而把程序运行在一个隔离的、密闭的、隐私的空间内，对外整体提供服务。（可以把容器理解为一个全新的电脑（操作系统））**



业务逻辑：

Docker 负责运行 Java(class 文件)，给出运行结果。

1. 把用户的代码保存为文件
2. 编译代码，得到 class 文件
3. 将编译好的文件（class 文件）上传至 Docker 容器中
4. 在 Docker 容器中执行代码，得到输出结果
5. 收集整理输出结果
6. 文件清理，释放空间
7. 错误处理，提升程序健壮性




