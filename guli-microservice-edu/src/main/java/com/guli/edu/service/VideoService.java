package com.guli.edu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.guli.edu.entity.Video;
import com.guli.edu.form.VideoInfoForm;

/**
 * <p>
 * 课程视频 服务类
 * </p>
 *
 * @author Helen
 * @since 2019-06-24
 */
public interface VideoService extends IService<Video> {

	void saveVideoInfo(VideoInfoForm videoInfoForm);

	VideoInfoForm getVideoInfoFormById(String id);

	void updateVideoInfoById(VideoInfoForm videoInfoForm);

	void removeVideoById(String id);
}
