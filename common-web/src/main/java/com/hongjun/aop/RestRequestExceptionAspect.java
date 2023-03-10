package com.hongjun.aop;

import cn.hutool.json.JSONUtil;
import com.hongjun.enums.EnumBusinessError;
import com.hongjun.error.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

/**
 * @author hongjun500
 * @date 2023/2/1 15:05
 * @tool ThinkPadX1隐士
 * Created with 2022.2.2.IntelliJ IDEA
 * Description: rest请求异常时的参数和响应
 */
@Aspect
@Component
@Order(value = 2)
@Log4j2
public class RestRequestExceptionAspect {
	/**
	 * 切入点：RestController
	 */
	@Pointcut(value = "@annotation(org.springframework.web.bind.annotation.RestController)")
	public void annotationPointCut() {
	}

	/**
	 * 环绕通知
	 */
	@Around(value = "annotationPointCut()")
	public Object around(ProceedingJoinPoint pjp) throws Throwable {
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
		HttpServletRequest request = servletRequestAttributes.getRequest();
		Object[] reqArgs = pjp.getArgs();
		log.debug("请求的地址：{},\n请求的参数{}", request.getRequestURI(), Arrays.toString(reqArgs));
		try {
			Object proceed = pjp.proceed(reqArgs);
			log.debug("响应结果：{}", JSONUtil.toJsonStr(proceed));
		} catch (BusinessException businessException) {
			String errMsg = businessException.getErrMsg();
			log.debug("业务异常{}\t exception:{}", parseMethodInfo(pjp), errMsg);
			BusinessException.assertBusinessException(EnumBusinessError.UNKNOWN_ERROR, "业务异常");
		}
		return null;
	}

	private String parseMethodInfo(ProceedingJoinPoint joinPoint) {
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		Object[] args = joinPoint.getArgs();
		String[] params = methodSignature.getParameterNames();
		StringBuilder builder = new StringBuilder(joinPoint.getSignature().toString() + "参数: ");
		try {
			for (int i = 0; i < args.length; i++) {
				builder.append(",").append(params[i]).append(":").append(JSONUtil.toJsonStr(args[i]));
			}
		} catch (Throwable ex) {
			//ignore
		}

		return builder.toString();
	}
}
