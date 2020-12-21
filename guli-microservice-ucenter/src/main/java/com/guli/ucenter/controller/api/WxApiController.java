package com.guli.ucenter.controller.api;

import com.google.gson.Gson;
import com.guli.common.constants.ResultCodeEnum;
import com.guli.common.exception.GuliException;
import com.guli.common.vo.R;
import com.guli.ucenter.entity.Member;
import com.guli.ucenter.service.MemberService;
import com.guli.ucenter.util.ConstantPropertiesUtil;
import com.guli.ucenter.util.CookieUtils;
import com.guli.ucenter.util.HttpClientUtils;
import com.guli.ucenter.util.JwtUtils;
import com.guli.ucenter.vo.LoginInfoVo;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * @author helen
 * @since 2019/7/7
 */
@CrossOrigin
@Controller
@RequestMapping("/api/ucenter/wx")
public class WxApiController {

	@Autowired
	private MemberService memberService;

	/**
	 * 显示登录二维码
	 * @return
	 */
	@GetMapping("login")
	public String genQrConnect(HttpSession session){

		System.out.println("请求微信OAuth2.0授权登录");

		//1、微信开放平台的授权url
		String baseUrl = "https://open.weixin.qq.com/connect/qrconnect" +
				"?appid=%s" +
				"&redirect_uri=%s" +
				"&response_type=code" +
				"&scope=snsapi_login" +
				"&state=%s" +
				"#wechat_redirect";

		//2、回调地址
		String redirectUrl = ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL;
		try {
			redirectUrl = URLEncoder.encode(redirectUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new GuliException(ResultCodeEnum.URL_ENCODE_ERROR);
		}


		//3、生成state参数
		String state = "imhelen"; //正式的生产环境中，需要生成一个随机字符串
//		String state = UUID.randomUUID().toString().replaceAll("-", "");
		session.setAttribute("wx-open-state", state);
		System.out.println("生成 state = " + state);

		//4、生成qrUrl
		String qrcodeUrl = String.format(
				baseUrl,
				ConstantPropertiesUtil.WX_OPEN_APP_ID,
				redirectUrl,
				state
		);


		return "redirect:" + qrcodeUrl;
	}


	/**
	 * 授权回调接口
	 */
	@GetMapping("callback")
	public String callback(String code,
						   String state,
						   HttpSession session,
						   HttpServletRequest request,
						   HttpServletResponse response){

		System.out.println("回调接口被调用");

		System.out.println("授权临时票据 code = " + code);
		System.out.println("state参数 state = " + state);

		//判断回调参数是否合法
		String stateStr = (String)session.getAttribute("wx-open-state");
		System.out.println("session 中的state参 state = " + stateStr);
		if(StringUtils.isEmpty(code) || StringUtils.isEmpty(state) || !state.equals(stateStr)){
			throw new GuliException(ResultCodeEnum.ILLEGAL_CALLBACK_REQUEST_ERROR);
		}

		//发送通过code获取access_token的请求
		String baseAccessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token" +
				"?appid=%s" +
				"&secret=%s" +
				"&code=%s" +
				"&grant_type=authorization_code";

		String accessTokenUrl = String.format(
				baseAccessTokenUrl,
				ConstantPropertiesUtil.WX_OPEN_APP_ID,
				ConstantPropertiesUtil.WX_OPEN_APP_SECRET,
				code
		);

		//向授权服务器申请授权码：向围殴新服务器发送远程http请求
		String result = null;
		try {
			//获得响应的json字符串
			result = HttpClientUtils.get(accessTokenUrl);
		} catch (Exception e) {
			throw new GuliException(ResultCodeEnum.FETCH_ACCESS_TOKEN_ERROR);
		}

		//将json字符串转换成java对象
		Gson gson = new Gson();
		HashMap<String, Object> resultMap = gson.fromJson(result, HashMap.class);
		if(resultMap.get("errcode") != null){
			throw new GuliException(ResultCodeEnum.FETCH_ACCESS_TOKEN_ERROR);
		}

		//从返回结果中获取access_token和用户的openid
		String accessToken = (String)resultMap.get("access_token");
		String openid = (String)resultMap.get("openid");

		if(StringUtils.isEmpty(accessToken) || StringUtils.isEmpty(openid)){
			throw new GuliException(ResultCodeEnum.FETCH_ACCESS_TOKEN_ERROR);
		}
		System.out.println("accessToken = " + accessToken);
		System.out.println("openid = " + openid);

		//根据openid查询用户在数据库中是否存在
		System.out.println("根据openid查询用户在数据库中是否存在");

		Member member = memberService.getByOpenid(openid);

		if(member == null){//新用户
			System.out.println("注册微信新用户");

			String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
					"?access_token=%s" +
					"&openid=%s";
			String userInfoUrl = String.format(
					baseUserInfoUrl,
					accessToken,
					openid);
			//向微信服务器发送远程请求
			String resultuserInfo = null;
			try {
				resultuserInfo = HttpClientUtils.get(userInfoUrl);
			} catch (Exception e) {
				throw new GuliException(ResultCodeEnum.FETCH_USER_INFO_ERROR);
			}

			HashMap<String, Object> resultUserInfoMap = gson.fromJson(resultuserInfo, HashMap.class);
			if(resultUserInfoMap.get("errcode") != null){
				throw new GuliException(ResultCodeEnum.FETCH_USER_INFO_ERROR);
			}

			String nickname = (String)resultUserInfoMap.get("nickname");
			String headimgurl = (String)resultUserInfoMap.get("headimgurl");

			member = new Member();
			member.setNickname(nickname);
			member.setAvatar(headimgurl);
			member.setOpenid(openid);
			memberService.save(member);
		}

		//登录
		System.out.println("登录：生成jwt令牌，传到客户端存储");
		//生成jwt token
		String jwtToken = JwtUtils.generateJWT(member);
		//将jwt token存入cookie
//		CookieUtils.setCookie(
//				request,
//				response,
//				"guli_jwt_token",
//				jwtToken,
//				60 * 30);

		//跳转到网站的主页面
		return "redirect:http://localhost:3000?token=" + jwtToken;
	}

	@GetMapping("get-jwt")
	@ResponseBody
	public R getJwt(HttpServletRequest request){

		String jwtToken = CookieUtils.getCookieValue(request, "guli_jwt_token");
		return R.ok().data("guli_jwt_token", jwtToken);
	}

	@PostMapping("parse-jwt")
	@ResponseBody
	public R getLoginInfoByJwtToken(@RequestBody String jwtToken){

		Claims claims = JwtUtils.checkJWT(jwtToken);

		String id = (String)claims.get("id");
		String nickname = (String)claims.get("nickname");
		String avatar = (String)claims.get("avatar");

		//根据用户id查询用户授权信息或会员级别
		LoginInfoVo loginInfoVo = new LoginInfoVo();
		loginInfoVo.setId(id);
		loginInfoVo.setNickname(nickname);
		loginInfoVo.setAvatar(avatar);

		return R.ok().data("loginInfo", loginInfoVo);
	}
}
