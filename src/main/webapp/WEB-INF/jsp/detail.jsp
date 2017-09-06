<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%-- 秒杀详情页，秒杀操作执行页面 --%>

<!DOCTYPE html>
<html>
<head>
    <title>秒杀详情页</title>
    <%@include file="common/head.jsp"%>
</head>
<body>

    <div class="container">
        <div class="panel panel-default text-center">
            <div class="panel-heading">
                <h1>${spike.name}</h1>
            </div>
            <div class="panel-body">
                <h2 class="text-danger">
                    <%-- 显示 time 图标 --%>
                    <span class="glyphicon glyphicon-time"></span>
                    <%-- 展示倒计时 --%>
                    <span class="glyphicon" id="spike-box"></span>
                </h2>
            </div>
        </div>
    </div>

    <%-- 登陆弹出层，输入电话 --%>
    <div id="spikePhoneModal" class="modal fade">
        <div class="modal-dialog">
            <div class="modal-content">

                <div class="modal-header">
                    <h3 class="modal-title text-center">
                        <span class="glyphicon glyphicon-phone"></span>秒杀电话
                    </h3>
                </div>

                <div class="modal-body">
                    <div class="row">
                        <div class="col-xs-8 col-xs-offset-2">
                            <input type="text" name="spikePhone" id="spikePhoneKey"
                                   placeholder="填写手机号" class="form-control"/>
                        </div>
                    </div>
                </div>

                <div class="modal-footer">
                    <%-- 验证信息 --%>
                    <span id="spikePhoneMessage" class="glyphicon"></span> <%-- 用于显示错误信息 --%>
                    <button type="button" id="spikePhoneBtn" class="btn btn-success">
                        <span class="glyphicon glyphicon-phone"></span>
                        Submit
                    </button> <%-- 确认按钮 --%>
                </div>

            </div>
        </div>
    </div>

</body>

<!-- jQuery 文件。务必在 bootstrap.min.js 之前引入 -->
<script src="https://cdn.bootcss.com/jquery/2.1.1/jquery.min.js"></script>
<!-- 最新的 Bootstrap 核心 JavaScript 文件 -->
<script src="https://cdn.bootcss.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>

<%-- 使用 CDN 获取公共js http://www.bootcdn.cn/ --%>
<%-- jqeury cookie 操作插件 --%>
<script src="http://cdn.bootcss.com/jquery-cookie/1.4.1/jquery.cookie.min.js"></script>
<%-- jquery countdown 倒计时插件 --%>
<script src="https://cdn.bootcss.com/jquery.countdown/2.2.0/jquery.countdown.min.js"></script>

<%--
    <script src="https://cdn.bootcss.com/jquery.countdown/2.1.0/jquery.countdown.min.js"></script>
    <script src="<%=request.getContextPath()%>/webjars/jquery.countdown/2.1.0/dist/jquery.countdown.min.js"></script>
--%>


<%-- 交互逻辑 --%>
<script src="/resources/script/spike.js" type="text/javascript"></script>
<script type="text/javascript">
    $(function () {
        // 使用EL表达式传入参数
        // 初始化详情页面
       spike.detail.init({
           spikeId : ${spike.spikeId},
           startTime : ${spike.startTime.time}, // 毫秒时间
           endTime : ${spike.endTime.time}
       });
    });
</script>
</html>
