create table batch_upload_file
(
    id          int auto_increment comment '主键ID'
        primary key,
    task_id     varchar(36)                           not null comment '任务ID',
    file_name   varchar(255)                          not null comment '文件名',
    file_path   varchar(500)                          null comment '文件存储路径',
    file_size   bigint      default 0                 not null comment '文件大小(字节)',
    status      varchar(20) default 'pending'         not null comment '文件处理状态(pending,processing,success,failed)',
    message     varchar(500)                          null comment '处理信息',
    result_data text                                  null comment '处理结果数据(JSON格式)',
    created_at  timestamp   default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at  timestamp   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '批量上传文件详情表' charset = utf8mb4;

create index idx_status
    on batch_upload_file (status);

create index idx_task_id
    on batch_upload_file (task_id);

create table batch_upload_task
(
    id            varchar(36)                           not null comment '任务ID'
        primary key,
    total_files   int         default 0                 not null comment '总文件数',
    success_count int         default 0                 not null comment '成功处理数',
    failed_count  int         default 0                 not null comment '失败处理数',
    status        varchar(20) default 'pending'         not null comment '任务状态(pending,processing,completed,failed)',
    progress      int         default 0                 not null comment '处理进度(0-100)',
    message       varchar(500)                          null comment '任务信息',
    created_at    timestamp   default CURRENT_TIMESTAMP not null comment '创建时间',
    updated_at    timestamp   default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '批量上传任务表' charset = utf8mb4;

create index idx_created_at
    on batch_upload_task (created_at);

create index idx_status
    on batch_upload_task (status);

create table feedback
(
    id      int auto_increment comment '反馈ID'
        primary key,
    name    varchar(50)  not null comment '用户姓名',
    email   varchar(100) not null comment '用户邮箱',
    rating  tinyint(1)   not null comment '评分(1-5星)',
    comment text         null comment '反馈内容',
    date    date         not null comment '反馈日期',
    user_id int          null comment '用户id'
)
    comment '用户反馈表' charset = utf8mb4;

create index idx_email
    on feedback (email);

create table papers
(
    id                bigint auto_increment comment '论文ID'
        primary key,
    title             varchar(500)                       null comment '论文标题',
    author            varchar(200)                       null comment '作者',
    abstract          text                               null comment '摘要',
    content           longtext                           null comment '论文全文内容',
    file_path         varchar(500)                       null comment '文件路径',
    file_type         varchar(10)                        null comment '文件类型 (PDF, DOC, DOCX, TXT)',
    upload_time       datetime                           null comment '上传时间',
    word_count        int      default 0                 null comment '字数统计',
    minhash_signature longtext                           null,
    created_at        datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updated_at        datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted           tinyint  default 0                 null comment '逻辑删除标记 (0:未删除, 1:已删除)',
    tf_idf_vector     longtext                           null,
    keywords          longtext                           null,
    category          varchar(100)                       null comment '论文分类标签',
    complexity_score  double                             null comment '文本复杂度评分'
)
    comment '论文表';

create index idx_author
    on papers (author);

create index idx_created_at
    on papers (created_at);

create index idx_deleted
    on papers (deleted);

create index idx_file_type
    on papers (file_type);

create index idx_paper_category
    on papers (category);

create index idx_title
    on papers (title);

create index idx_upload_time
    on papers (upload_time);

create table sentences
(
    id                bigint auto_increment comment '句子ID'
        primary key,
    paper_id          bigint                             not null comment '所属论文ID',
    sentence_text     longtext                           not null comment '句子文本内容',
    sentence_index    int                                not null comment '句子在论文中的索引位置',
    word_count        int      default 0                 null comment '句子字数',
    minhash_signature longtext                           null,
    lsh_bucket        longtext                           null,
    created_at        datetime default CURRENT_TIMESTAMP null comment '创建时间',
    sentence_vector   longtext                           null comment '句子向量的Base64编码',
    key_terms         longtext                           null comment '关键术语集合',
    semantic_hash     varchar(255)                       null comment '语义哈希值'
)
    comment '句子表';

create index idx_created_at
    on sentences (created_at);

create index idx_lsh_bucket
    on sentences (lsh_bucket(100));

create index idx_minhash
    on sentences (minhash_signature(100));

create index idx_paper_id
    on sentences (paper_id);

create index idx_sentence_index
    on sentences (sentence_index);

create index idx_sentence_semantic_hash
    on sentences (semantic_hash);

create table user
(
    id          bigint auto_increment comment '用户ID（主键）'
        primary key,
    name        varchar(50)                          null comment '用户名',
    user_name   varchar(50)                          not null comment '账号（登录用）',
    password    varchar(100)                         not null comment '密码（建议存储加密后的值）',
    is_manage   tinyint(1) default 0                 not null comment '是否为管理员（0-否，1-是）',
    create_time datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    constraint idx_username
        unique (user_name) comment '账号唯一索引'
)
    comment '用户信息表';

create table words
(
    id           bigint auto_increment comment '词ID'
        primary key,
    sentence_id  bigint                               not null comment '所属句子ID',
    word_text    varchar(100)                         not null comment '词文本',
    word_index   int                                  not null comment '词在句子中的索引位置',
    is_stop_word tinyint(1) default 0                 null comment '是否为停用词',
    created_at   datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    deleted      tinyint    default 0                 null comment '逻辑删除标记 (0:未删除, 1:已删除)'
)
    comment '分词表';

create index idx_created_at
    on words (created_at);

create index idx_deleted
    on words (deleted);

create index idx_is_stop_word
    on words (is_stop_word);

create index idx_sentence_id
    on words (sentence_id);

create index idx_word_index
    on words (word_index);

create index idx_word_text
    on words (word_text);


