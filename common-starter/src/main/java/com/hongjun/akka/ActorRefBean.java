package com.hongjun.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.hongjun.akka.SpringExtension.SpringExtProvider;

/**
 * @author hongjun500
 * @date 2023/2/10 17:03
 * @tool ThinkPadX1隐士
 * Created with 2022.2.2.IntelliJ IDEA
 * Description:
 */
@Data
@Log4j2
public class ActorRefBean {
	private static ActorSystem actorSystem;
	private static Map<String, ActorRef> actorRefMap = new ConcurrentHashMap<>();

	public ActorRefBean(ActorSystem actorSystem) {
		ActorRefBean.actorSystem = actorSystem;
	}

	public void tell(Class tclass, Object msg){
		BaseActorMsg baseActorMsg = new BaseActorMsg();
		baseActorMsg.setObject(msg);
		String beanName = tclass.getSimpleName();
		Component component = AnnotationUtil.getAnnotation(tclass, Component.class);
		if (component != null && StrUtil.isNotEmpty(component.value())) {
			beanName = component.value();
		}
		ActorRef actorRef = actorRefMap.get(beanName);
		if (actorRef == null) {
			actorRef = initActorRef(beanName);
		}
		if (actorRef == null) {
			return;
		}
		actorRef.tell(baseActorMsg, ActorRef.noSender());
	}

	private synchronized ActorRef initActorRef(String beanName){
		// double check，确保actor不会被重复初始化
		ActorRef actorRef = actorRefMap.get(beanName);
		if(Objects.isNull(actorRef)){
			try {
				actorRef = actorSystem.actorOf(SpringExtProvider.get(actorSystem).props(beanName), beanName);
				actorRefMap.putIfAbsent(beanName, actorRef);
			} catch (Exception e) {
				log.error("initActorRef初始化错误---{}", e.getMessage());
			}
		}
		return actorRef;
	}
}