package com.guli.ucenter.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author helen
 * @since 2019/7/7
 */
@Data
public class LoginInfoVo implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;
	private String nickname;
	private String avatar;
}
