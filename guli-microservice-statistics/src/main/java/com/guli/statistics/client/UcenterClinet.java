package com.guli.statistics.client;

import com.guli.common.vo.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author helen
 * @since 2019/7/1
 */
@Component
@FeignClient("guli-ucenter")
public interface UcenterClinet {

	@GetMapping(value = "/admin/ucenter/member/count-register/{day}")
	public R registerCount(
			@PathVariable("day") String day);

}
