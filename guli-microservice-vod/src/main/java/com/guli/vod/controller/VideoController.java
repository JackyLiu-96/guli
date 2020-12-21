package com.guli.vod.controller;

import com.guli.common.vo.R;
import com.guli.vod.service.VideoService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author helen
 * @since 2019/7/6
 */
@Api(description="阿里云视频点播微服务")
@CrossOrigin //跨域
@RestController
@RequestMapping("/vod/video")
public class VideoController {


	@Autowired
	private VideoService videoService;

	@GetMapping("get-play-auth/{videoId}")
	public R getVideoPlayAuth(@PathVariable String videoId){

		String playAuth = videoService.getVideoPlayAuth(videoId);
		return R.ok().message("获取播放凭证成功").data("playAuth", playAuth);
	}
}
