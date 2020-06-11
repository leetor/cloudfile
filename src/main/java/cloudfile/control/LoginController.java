package cloudfile.control;

import javax.servlet.http.HttpServletRequest;

import cloudfile.RSAEncrypt;
import cloudfile.bean.User;
import cloudfile.utils.PR;
import cloudfile.utils.StrUtil;
import cloudfile.interceptor.LoginUserAuthInterceptor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cloudfile.exceptionhandler.CustomLogicException;

@Controller
@RequestMapping("/login")
public class LoginController {

	/**
	 * 获取公钥
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public PR getpubkey() {
		return new PR("", RSAEncrypt.getInstance().getPubKey());
	}

	/**
	 * 登录验证
	 * 
	 * @param request
	 * @return
	 * @throws CustomLogicException
	 */
	@RequestMapping(method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public PR auth(HttpServletRequest request) throws CustomLogicException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		if (StrUtil.isNullOrEmpty(username) || StrUtil.isNullOrEmpty(password)) {
			throw new CustomLogicException(401, "参数非法【用户名或密码为空】", null);
		}
		username = RSAEncrypt.getInstance().decryptByPriKey(username);
		password = RSAEncrypt.getInstance().decryptByPriKey(password);
		User u = User.loginAuth(username, password);
		if (u != null) {
			String token = u.createToken(request);
			return new PR("登录成功", token);
		}
		throw new CustomLogicException(401, "用户名或密码错误", null);
	}

	/**
	 * 退出登录
	 * 
	 * @return
	 */
	@RequestMapping(value = "/logout", method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public PR logout(@RequestAttribute(LoginUserAuthInterceptor.LOGIN_USER) User loginUser) {
		loginUser.setToken(null);
		loginUser.setExpirationtime(-1);
		return new PR("ok", null);
	}
}
