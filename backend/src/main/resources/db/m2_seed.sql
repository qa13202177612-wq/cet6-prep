-- M2 种子:长难句 / 听力句 / 阅读篇+题 / 语法题 / 作文题
USE cet6;

-- 长难句 15 条(真题风格仿写)
INSERT INTO t_sentence (content, grammar_point, structure_tip, translation, source_year) VALUES
('The study suggests that what matters most is not how much you read, but how attentively you engage with the text.', '主语从句+表语从句', '主干: The study suggests that... | that 从句内: what matters most(主) is(系) not A but B(表)', '研究指出,最重要的不是你读了多少,而是你有多专注地与文本互动。', 2023),
('Hardly had the meeting begun when the fire alarm went off, forcing everyone out of the building.', '倒装+分词作状语', 'Hardly...when... 固定搭配,主句部分倒装 had the meeting begun;forcing... 结果状语', '会议刚开始火警就响了,把所有人都赶出了大楼。', 2022),
('It is the employees who work overtime regularly, rather than the company itself, that suffer the long-term consequences.', '强调句', 'It is X that... 强调结构,X = the employees...,rather than 插入语', '承受长期后果的正是那些定期加班的员工,而非公司本身。', 2023),
('The more exposure children have to diverse cultures, the better equipped they become at resolving conflicts peacefully.', 'the more 比较结构', 'The more A, the better B;be equipped at 擅长', '孩子接触的文化越多元,就越善于以和平方式解决冲突。', 2021),
('Not until the data were independently verified did the researchers dare to publish their controversial findings.', 'not until 倒装', 'Not until 位于句首主句倒装:did the researchers dare', '直到数据被独立验证,研究人员才敢发表这一争议性发现。', 2022),
('Climate models, however sophisticated, remain simplifications of a reality too complex to simulate in full.', '让步+too...to', 'however sophisticated = no matter how;主语 remain simplifications', '气候模型无论多精密,终究是对过于复杂而无法完整模拟的现实的简化。', 2024),
('What surprised the panel was not his answer itself but the ease with which he had arrived at it.', '表语从句+介词定语', 'the ease with which... 定语从句,with which 指 arrived at it 的方式', '令评委惊讶的不是他的答案,而是他得出答案时那份从容。', 2023),
('There is no denying that the gig economy has reshaped people’s attitudes toward job security.', '同位语从句', 'There is no denying that... that 从句作 denying 的宾语内容', '不可否认,零工经济已重塑了人们对工作稳定性的态度。', 2021),
('Had the policy been implemented earlier, thousands of small businesses would have survived the recession.', '虚拟倒装', 'If 省略倒装:Had the policy been = If the policy had been', '若政策早一点落地,数千家小企业本可挺过衰退。', 2022),
('Sleep researchers have found that pulling an all-nighter impairs performance in ways remarkably similar to drunkenness.', '宾语从句+介词短语', 'in ways similar to 以…的方式', '睡眠研究者发现,熬夜导致的机能损伤与醉酒惊人地相似。', 2024),
('The museum, which houses over 2,000 artifacts donated by private collectors, opens to the public next spring.', '非限制性定语从句', 'which 指代 the museum,插入补充信息', '这座收藏了两千余件私人捐赠文物的博物馆将于明年春对外开放。', 2021),
('So deeply was the audience moved by the speech that a respectful silence followed its conclusion.', 'so...that 倒装', 'So+adj 置句首主句倒装 was the audience moved', '演讲深深打动了听众,以至于结束后全场肃然无声。', 2023),
('Employers value candidates who can demonstrate critical thinking over those who merely list their qualifications.', '定语从句对比', 'value A over B;两个 who 从句分别修饰 candidates/those', '雇主更看重能展示批判性思维的求职者,而非仅仅罗列资历的人。', 2024),
('The hypothesis that language shapes thought has gained ground again thanks to fresh neuroimaging evidence.', '同位语从句', 'that 从句解释 hypothesis 内容;gain ground 获得支持', '「语言塑造思维」这一假说因新的神经影像证据而再度获得支持。', 2022),
('No sooner had the new app hit the charts than copycats flooded the store, diluting its appeal.', 'no sooner...than 倒装', 'No sooner had...than... 主句倒装', '新应用刚冲上榜单,仿制品便涌入商店,稀释了它的吸引力。', 2024);

-- 听力精听句 12 条(讲座/新闻片段改写)
INSERT INTO t_listening (title, section, duration_sec) VALUES ('校园讲座·拖延症','讲座',120), ('新闻短播·远程办公','新闻',90);
INSERT INTO t_listening_line (listening_id, seq, en_text, cn_text) VALUES
(1,1,'Procrastination is not a time-management problem; it is an emotion-management one.','拖延不是时间管理问题,而是情绪管理问题。'),
(1,2,'Students who delay studying report brief relief, followed by far greater anxiety.','拖延复习的学生会获得短暂解脱,随后焦虑成倍放大。'),
(1,3,'One effective strategy is to break a large task into embarrassingly small steps.','一个有效的策略是把大任务拆分成小得不好意思的程度。'),
(1,4,'The brain resists starting, far more than it resists continuing.','大脑抗拒的是开始,而不是继续。'),
(1,5,'Deadlines that you set for yourself, oddly enough, do actually work.','说来奇怪,自己给自己定的期限确实有效。'),
(1,6,'Perfectionism disguised as high standards is often procrastination in a fancy suit.','伪装成高标准完美主义,往往是拖延症穿了体面的外衣。'),
(2,1,'The share of fully remote positions has leveled off after two years of rapid growth.','完全远程岗位的比例在两年快速增长后趋于平稳。'),
(2,2,'Many firms now require staff in the office at least three days a week.','许多公司现在要求员工每周至少三天到岗。'),
(2,3,'Commuters report that flexible schedules matter more than free lunches.','通勤者表示弹性作息比免费午餐更有吸引力。'),
(2,4,'Urban cafés near office districts have become the new coworking spaces.','办公区附近的咖啡馆成了新的共享办公空间。'),
(2,5,'Analysts caution that productivity data from remote work remains mixed.','分析师提醒,远程办公的生产力数据仍喜忧参半。'),
(2,6,'The hybrid model, they say, is less a compromise than a permanent shift.','他们表示,混合模式与其说是妥协,不如说是一次永久性转变。');

-- 阅读:1 篇选文 + 4 题
INSERT INTO t_reading_passage (passage_type, title, content, source_year) VALUES
('仔细阅读','The Real Cost of Free Apps','大多数用户从未想过,为什么一款应用可以完全免费。答案早已是老生常谈:如果你不是付费用户,你就是产品。免费应用通过收集行为数据出售广告位盈利,而用户对这一模式的默认接受,让"同意条款"成了数字时代最美丽的谎言。最近一项研究发现,用户在安装应用时平均只花 11 秒浏览隐私条款——几乎等同于没有阅读。这种"隐私悖论"并非因为人们愚蠢,而是权衡结构出了问题:收益(即时、确定)在眼前,代价(未来、概率)很遥远。研究者提出,除非默认设置转向隐私友好一侧,否则任何呼吁用户"更小心"的道德说教都无济于事。', 2024);
INSERT INTO t_question (biz_type, ref_id, seq, stem, options, answer, analysis) VALUES
('READING',1,1,'According to the passage, free apps mainly profit by ____。','["A. selling subscriptions to premium features","B. monetizing user data through advertising","C. charging developers listing fees","D. donating computing resources to charities"]','B','首段"出售广告位盈利"→ 靠数据变现。'),
('READING',1,2,'The "11 seconds" finding is cited to show that ____。','["A. users read terms more carefully than expected","B. scanning speed has improved over time","C. users effectively ignore privacy terms","D. terms are written in simple language"]','C','11 秒≈没读,论证用户对条款形同无视。'),
('READING',1,3,'"隐私悖论"产生的根源在于 ____。','["A. 用户缺乏基本判断力","B. 收益即时而代价延后","C. 法律禁止收集数据","D. 应用体积过大"]','B','第二段明确:权衡结构问题——收益即时确定、代价未来概率;A 与"并非因为人们愚蠢"矛盾。'),
('READING',1,4,'研究者认为真正有效的方法是 ____。','["A. 教育用户细读条款","B. 惩罚违规企业","C. 加快条款阅读速度","D. 让隐私友好成为默认设置"]','D','末句"除非默认设置转向隐私友好一侧…否则无济于事"。');

-- 语法选择题 8 道(专项+模考拼卷用)
INSERT INTO t_question (biz_type, seq, stem, options, answer, analysis) VALUES
('GRAMMAR',1,'Not until the 1930s ____ into the field of molecular biology.','["A. scientists did venture","B. did scientists venture","C. scientists ventured","D. ventured scientists"]','B','not until 置句首,主句部分倒装:助动词提前。'),
('GRAMMAR',2,'The report recommends that the deadline ____ extended by two weeks.','["A. is","B. to be","C. be","D. will be"]','C','recommend 后虚拟语气 should+动词原形,should 可省,故选原形 be。'),
('GRAMMAR',3,'____ the budget deficit, the committee voted against the proposal.','["A. Given","B. Give","C. To give","D. Giving"]','A','given 作介词性分词"考虑到",表被动语境。'),
('GRAMMAR',4,'It is high time you ____ a serious job.','["A. find","B. found","C. will find","D. have found"]','B','It is (high) time + 过去式虚拟语气。'),
('GRAMMAR',5,'The data ____ that the intervention has been effective.','["A. shows","B. show","C. showing","D. is showed"]','B','data 为复数名词(datum 复数),谓语用原形。'),
('GRAMMAR',6,'Scarcely ____ the office when the phone rang.','["A. I had reached","B. had I reached","C. I reached","D. did I reach"]','B','scarcely...when 主句用过去完成且倒装。'),
('GRAMMAR',7,'There is no point ____ about what you cannot change.','["A. to worry","B. of worrying","C. in worrying","D. for worry"]','C','there is no point in doing 固定搭配。'),
('GRAMMAR',8,'The lawyer asked that the witness ____ immediately.','["A. releases","B. be released","C. released","D. will release"]','B','ask+that 从句用 (should) be done 虚拟,被动原形。');

-- 作文/翻译题 2 道
INSERT INTO t_writing_topic (type, title, prompt, ref_answer, source_year) VALUES
('ESSAY','图书馆该不该 24 小时开放','For this part, you are allowed 30 minutes to write an essay on whether university libraries should stay open 24 hours a day. You should give reasons and examples. (150-200 words)',
'开 24 小时利在公平与效率:考研学生、跨时区实验者受益;弊在能耗与管理成本。可行折中:考试季通宵、平日延长至午夜,并以预约自习舱控制成本。', 2023),
('TRANSLATION','数字经济段译','把下面中文翻译成英文:过去十年,中国移动支付规模增长了近三十倍,不仅改变了消费习惯,也催生了从外卖到共享出行的一系列新业态。',
'Over the past decade, mobile payment in China has grown nearly thirtyfold, transforming not only consumers’ habits but also giving rise to an entire range of new business models, from food delivery to shared mobility.', 2023);
