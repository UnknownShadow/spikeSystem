
// 主要交互逻辑代码
// javascript 模块化处理

var spike = {

    // 封装秒杀相关 ajax 的 url
    URL : {
        now : function () {
            // 当前系统时间
            return '/spike/time/current';
        },
        exposer : function (spikeId) {
            // 秒杀端口开启判断
            return '/spike/' + spikeId + '/exposer';
        },
        execution : function (spikeId, md5) {
            // 秒杀执行操作
            return '/spike/' + spikeId + '/' + md5 + '/execution';
        }
    },
    // 验证手机号
    validatePhone : function (phone) {
        // 秒杀登陆验证，非11位或不是纯数字，验证失败
        if (phone && phone.length == 11 && !isNaN(phone)){
            return true;
        } else {
            return false;
        }
    },
    handleSpike : function (spikeId, node) {
        // 处理秒杀逻辑
        // 获取秒杀地址，控制显示逻辑，执行秒杀
        node.hide()
            // 隐藏并增加秒杀按钮
            .html('<button class="btn btn-primary btn-large" id="spikeBtn">开始秒杀</button>'); // 按钮
        $.post(spike.URL.exposer(spikeId), {}, function (result) {
            // 在回调函数中，执行交互流程
            if (result && result['success']){
                var exposer = result['data'];
                if (exposer['exposer']) {
                    // 开启秒杀
                    // 获取秒杀地址
                    var md5 = exposer['md5'];
                    var spikeUrl = spike.URL.execution(spikeId, md5);
                    console.log("spikeUrl: " + spikeUrl);
                    // 绑定一次点击时间
                    $('#spikeBtn').one('click', function () {
                        // 执行秒杀请求
                        // 1: 禁用按钮
                        $(this).addClass('disable');
                        // 2： 发送秒杀请求, 执行秒杀
                        $.post(spikeUrl, {}, function (result) {
                            if (result && result['success']){
                                var spikeResult = result['data'];
                                var state = spikeResult['state'];
                                var stateInfo = spikeResult['stateInfo'];
                                // 显示秒杀结果
                                node.html('<span class="label label-success">' + stateInfo + '</span>');
                            } else {
                                console.log("result : " + result);
                            }
                        });
                    });
                    node.show();
                } else {
                    // 未开启秒杀
                    var current = exposer['current'];
                    var start = exposer['start'];
                    var end = exposer['end'];
                    // 重新计算计时逻辑
                    spike.countdown(spikeId, current, start, end);
                }
            } else {
                console.log('result: ' + result);
            }
        });
    },
    countdown: function (spikeId, currentTime, startTime, endTime) {
        var spikeBox = $('#spike-box');
        // 时间判断
        if (currentTime > endTime){
            // 秒杀结束
            spikeBox.html('秒杀结束！');
        } else if (currentTime < startTime){
            // 秒杀未开始，计时事件绑定
            // 加一秒防止时间偏移
            var spikeTime = new Date(startTime + 1000);
            spikeBox.countdown(spikeTime, function (event) {
                // 控制时间格式
                var format = event.strftime('秒杀倒计时：%D天 %H时 %M分 %S秒');
                spikeBox.html(format);
                /*时间完成后回调事件*/
            }).on('finish.countdown', function () {
                // 获取秒杀地址，控制显示逻辑，执行秒杀
                spike.handleSpike(spikeId, spikeBox);
            });
        } else {
            // 秒杀已经开始
            spike.handleSpike(spikeId, spikeBox);
        }
    },
    // 详情页秒杀逻辑
    detail : {
        // 详情页初始化
        init : function (params) {
            // 手机验证和登陆， 计时交互
            // 规划交互流程
            // 在cookie中查找手机号
            var spikePhone = $.cookie('spikePhone');

            // 验证手机号
            if (!spike.validatePhone(spikePhone)){
                // 绑定phone
                // 控制输出
                var spikePhoneModel = $('#spikePhoneModal');

                spikePhoneModel.modal({
                    show:true, // 显示弹出层
                    backdrop:'static', // 禁止位置关闭
                    keyboard:false // 关闭键盘事件
                });

                // 弹出层上的按钮
                $('#spikePhoneBtn').click(function (){
                    // 获取输入手机号，并验证
                    var inputPhone = $('#spikePhoneKey').val();
                    if (spike.validatePhone(inputPhone)){
                        // 电话写入cookie， 有效期7天，在 /spike 路径内有效
                        $.cookie('spikePhone', inputPhone, {expires: 7, path: '/spike'});
                        // 刷新页面
                        window.location.reload();
                    } else {
                        // 增加错误信息，显示错误信息弹出效果
                        $('#spikePhoneMessage').hide().html('<label class="label label-danger">手机号错误！</label>').show(300);
                    }
                });
            }

            // 已经登陆
            // 计时交互
            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var spikeId = params['spikeId'];
            /**
             *  使用 ajax 的 get 请求
             *  调用 controller 中 time 方法获取系统时间
             *  {} 无参数输入
             *  获得 time 封装为 JSON 的返回结果，进行操作
             */
            $.get(spike.URL.now(), {}, function (result) {
                if (result && result['success']){
                    var currentTime = result['data'];
                    console.log(currentTime);
                    // 时间判断, 调用 countdown 函数进行计时交互
                    spike.countdown(spikeId, currentTime, startTime, endTime);
                } else {
                    console.log('result: ' + result);
                }
            });
        }
    }
}
