package org.spike.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spike.Exception.RepeatSpikeException;
import org.spike.Exception.SpikeClosedException;
import org.spike.dto.Exposer;
import org.spike.dto.SpikeExecution;
import org.spike.dto.SpikeResult;
import org.spike.entity.Spike;
import org.spike.enums.SpikeStateEnum;
import org.spike.service.SpikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.jws.WebParam;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 *  Controller 类
 */

@Controller // 放入spring容器中
@RequestMapping("/spike") // url:/模块/资源/{id}/细分 -> /spike/list
public class SpikeController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SpikeService spikeService;

    /**
     *  返回列表页
     * @param model
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model){

        // 获取列表页
        List<Spike> list = spikeService.getSpikeList();
        model.addAttribute("list", list);

        // list.jsp + model = ModelAndView
        return "list";
    }

    /**
     *  返回详情页
     * @param spikeId
     * @param model
     * @return
     */
    @RequestMapping(value = "/{spikeId}/detail", method = RequestMethod.GET)
    public String detail(@PathVariable("spikeId") Long spikeId, Model model){
        // 判断是否获得spikeId
        if (spikeId == null)
            return "redirect:/spike/list";

        Spike spike = spikeService.getSpikeById(spikeId);
        if (spike == null)
            return "forward:/spike/list";

        // 加入秒杀商品参数用于 jsp 页面
        model.addAttribute("spike", spike);

        return "detail";
    }

    /**
     *  ajax json
     *  对应是否开启秒杀端口，获取返回类并封装为JSON
     * @param spikeId
     * @return
     */
    @RequestMapping(value = "/{spikeId}/exposer",
                    // 只响应 post 请求
                    method = RequestMethod.POST,
                    // 指定封装数据格式
                    produces = {"application/json;charset=UTF-8"})
    @ResponseBody // 将返回类型封装为json
    public SpikeResult<Exposer> exposer(@PathVariable Long spikeId){

        SpikeResult<Exposer> result;
        try {
            Exposer exposer = spikeService.exposeSpikeUrl(spikeId);
            result = new SpikeResult<Exposer>(true, exposer);
        } catch (Exception e){
            logger.error(e.getMessage(), e);
            result = new SpikeResult<Exposer>(false, e.getMessage());
        }

        return result;
    }

    /**
     *  返回秒杀执行结果
     * @param spikeId
     * @param md5
     * @param phoneNumber
     * @return
     */
    @RequestMapping(value = "/{spikeId}/{md5}/execution",
                    method = RequestMethod.POST,
                    produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SpikeResult<SpikeExecution> execute(@PathVariable("spikeId") Long spikeId,
                                               @PathVariable("md5") String md5,
                                               @CookieValue(value = "spikePhone", required = false) Long phoneNumber

    ){

        // 验证cookie避免空值
        if (phoneNumber == null)
            return new SpikeResult<SpikeExecution>(false, "未注册");

        SpikeResult<SpikeExecution> result;
        try {
            // 直接与数据库通信
            // SpikeExecution execution = spikeService.executeSpike(spikeId, phoneNumber, md5);
            // 通过存储过程获取
//            SpikeExecution execution = spikeService.executeSpikeByProcedure(spikeId, phoneNumber, md5);
            // 通过redis缓存获取
             SpikeExecution execution = spikeService.executeSpikeByRedisLock(spikeId, phoneNumber, md5);
            return new SpikeResult<SpikeExecution>(true, execution);
        } catch (RepeatSpikeException e){

            SpikeExecution execution = new SpikeExecution(spikeId, SpikeStateEnum.REPEATED_SPIKE);
            return new SpikeResult<SpikeExecution>(true, execution);
        } catch (SpikeClosedException e){

            SpikeExecution execution = new SpikeExecution(spikeId, SpikeStateEnum.END);
            return new SpikeResult<SpikeExecution>(true, execution);
        } catch (Exception e){

            logger.error(e.getMessage(), e);
            SpikeExecution execution = new SpikeExecution(spikeId, SpikeStateEnum.INNER_ERROR);
            return new SpikeResult<SpikeExecution>(true, execution);

        }
    }

    /**
     *  返回当前系统时间
     *  只响应 get 请求
     * @return
     */
    @RequestMapping(value = "/time/current", method = RequestMethod.GET)
    @ResponseBody
    public SpikeResult<Long> time(){
        Date date = new Date();
        return new SpikeResult<Long>(true, date.getTime());
    }
}
