package com.guli.aliyunoss.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author helen
 * @since 2019/6/28
 */
public interface FileService {

	String uploadFile(MultipartFile file);
}
