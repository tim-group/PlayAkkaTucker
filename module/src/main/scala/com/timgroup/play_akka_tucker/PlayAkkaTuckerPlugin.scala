package com.timgroup.play_akka_tucker

import play.api.{Logger, Application, Plugin}
import com.timgroup.play_tucker.PlayTuckerPlugin
import play.api.libs.concurrent.Akka
import akka.dispatch.{MessageDispatcherConfigurator, ExecutionContext, ExecutorServiceDelegate, Dispatcher}
import akka.jsr166y.ForkJoinPool
import com.timgroup.tucker.info.Component
import akka.actor.ActorSystem
import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions._

case class ExecutionContextIdentifier(val path: String, actorSystem: ActorSystem) {
  def name: String = {
    path.split('.').last
  }

  def lookup: ExecutionContext = {
    actorSystem.dispatchers.lookup(path)
  }
}

class PlayAkkaTuckerPlugin(application: Application) extends Plugin {

  override def onStart() {
    import play.api.Play.current
    import com.typesafe.plugin.use

    val tucker = use[PlayTuckerPlugin]

    getExecutionContextsFor(Akka.system)
      .flatMap(createComponent)
      .foreach(tucker.addComponent)

    Logger.info("PlayAkkaTuckerPlugin started")
  }

  private def createComponent(executionContextIdentifier: ExecutionContextIdentifier): Option[Component] = {
    getPool(executionContextIdentifier).map {
      pool => new ForkJoinPoolStatusComponent(executionContextIdentifier.name, pool)
    }
  }

  private def getPool(executionContextIdentifier: ExecutionContextIdentifier): Option[ForkJoinPool] = {

    try {
      val executionContext = executionContextIdentifier.lookup
      val dispatcher = executionContext.asInstanceOf[Dispatcher]
      val executorServiceField = dispatcher.getClass.getDeclaredField("executorService")
      executorServiceField.setAccessible(true)
      val atomicReference = executorServiceField.get(dispatcher)
      val executorServiceDelegate = atomicReference.asInstanceOf[java.util.concurrent.atomic.AtomicReference[Object]].get.asInstanceOf[ExecutorServiceDelegate]
      val forkJoinPool = executorServiceDelegate.executor.asInstanceOf[ForkJoinPool]
      Some(forkJoinPool)
    } catch {
      case e: Exception => {
        Logger.error("Error getting ForkJoinPool for EC %s: %s".format(executionContextIdentifier.name, e.getMessage))
        e.printStackTrace()
        None
      }
    }
  }

  private def getExecutionContextsFor(actorSystem: ActorSystem): Seq[ExecutionContextIdentifier] = {
    val field = actorSystem.dispatchers.getClass.getDeclaredField("dispatcherConfigurators")
    field.setAccessible(true)
    val dispatcherMap = field.get(actorSystem).asInstanceOf[ConcurrentHashMap[String, MessageDispatcherConfigurator]]

    dispatcherMap.keySet().toSeq.map(path => ExecutionContextIdentifier(path, actorSystem))
  }

  override def onStop() {
    Logger.info("PlayAkkaTuckerPlugin stopped")
  }

  def components = {
    Nil
  }
}
