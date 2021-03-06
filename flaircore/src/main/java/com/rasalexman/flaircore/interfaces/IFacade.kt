package com.rasalexman.flaircore.interfaces

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import com.rasalexman.flaircore.animation.LinearAnimator
import com.rasalexman.flaircore.controller.Controller
import com.rasalexman.flaircore.model.Model
import com.rasalexman.flaircore.patterns.facade.Facade
import com.rasalexman.flaircore.patterns.observer.Notification
import com.rasalexman.flaircore.patterns.observer.Observer
import com.rasalexman.flaircore.view.View

typealias FacadeInitializer = IFacade.() -> Unit

/**
 * Created by a.minkin on 21.11.2017.
 */
interface IFacade : INotifier {

    /**
     * Application context
     */
    val appContext: Context
    /**
     * Controller instance
     */
    val controller: IController
    /**
     * Model instance
     */
    val model: IModel
    /**
     * View instance
     */
    val view: IView

    /**
     * Static object holder
     */
    companion object : IMapper<IFacade> {
        /**
         * Default key for single facade core
         */
        const val DEFAULT_KEY = "DEFAULT_FACADE"
        /**
         * Global storage for all instance cores of IFacade
         */
        override val instanceMap = HashMap<String, IFacade>()

        /**
         * Facade Multiton Factory method.
         *
         * @param key
         * the name and multiton key for this IFacade core
         *
         * @param context
         * Application or Activity main classes
         *
         * @param init
         * init function for this core
         *
         * @return the Multiton core of the Facade
         */
        @Throws
        fun core(key: String = DEFAULT_KEY, context: Context? = null, init: FacadeInitializer? = null): IFacade {
            val facade = instance(key) {
                if (context == null) throw RuntimeException("You need to specified `context` for this core")
                Facade(key, if (context is Activity) context.applicationContext else context)
            }
            init?.let {
                facade.it()
            }
            return facade
        }

        /**
         * Check if a Core is registered or not.
         *
         * @param key the multiton key for the Core in question
         * @return whether a Core is registered with the given `key`.
         */
        @Synchronized
        fun hasCore(key: String = DEFAULT_KEY): Boolean = instanceMap.containsKey(key)

        /**
         * Remove a Core
         * @param key of the Core to remove
         */
        @Synchronized
        fun removeCore(key: String = DEFAULT_KEY) {
            // remove the model, view, controller
            // and facade instances for this key
            Model.removeModel(key)
            View.removeView(key)
            Controller.removeController(key)
            instanceMap.remove(key)
        }
    }
}

/////---------- INLINE SECTION -----///

/**
 * Retrieve an `IMediator` core from the `View`.
 *
 * @param mediatorName
 * Mediator name to retrieve
 *
 * @return the T : `IMediator` previously registered with the given
 * `mediatorName`.
 */
inline fun <reified T : IMediator> IFacade.retrieveMediator(mediatorName: String? = null): T = this.view.retrieveMediator(mediatorName)

/**
 * Register an `IMediator` with the `View` core.
 *
 * @param mediatorName
 * The name of the mediator to register with
 *
 * @param mediatorBuilder
 * Mediator instance builder function
 */
inline fun <reified T : IMediator> IFacade.registerMediator(mediatorName: String? = null, mediatorBuilder:()->T): T {
    return this.view.registerMediator(mediatorName, mediatorBuilder)
}

/**
 * Remove an `IMediator` from the `View` core.
 */
inline fun <reified T : IMediator> IFacade.removeMediator(mediatorName: String? = null): T? = this.view.removeMediator<T>(mediatorName) as? T

/**
 * Show last added IMediator from backstack. If there is no mediator in backstack show the one passed by generic type class
 *
 * @param animation
 * The animation instance to show last mediator
 */
inline fun <reified T : IMediator> IFacade.showLastOrExistMediator(animation: IAnimator? = null) {
    view.showLastOrExistMediator<T>(animation)
}

/**
 * Check if a IMediator is registered or not
 *
 * @param mediatorName
 * Optional mediator name to check for
 */
inline fun <reified T : IMediator> IFacade.hasMediator(mediatorName: String? = null): Boolean = view.hasMediator<T>(mediatorName)

/**
 * Show current selected mediator
 *
 * @param mapName
 * This is a current mediator name to put in backstack
 *
 * @param popLast
 * flag that indicates need to remove last showing from backstack
 *
 * @param animation
 * Instance of current animation
 */
inline fun <reified T : IMediator> IFacade.showMediator(mapName: String? = null, popLast: Boolean = false, animation: IAnimator? = null) {
    if (hasMediator<T>(mapName)) view.showMediator(mapName ?: T::class.toString(), popLast, animation)
    else retrieveMediator<T>(mapName).show(animation, popLast)
}

/**
 * Register an `IProxy` with the `Model` by name.
 *
 * @param proxyBuilder
 * Instance Builder function
 *
 * @return IProxy instance with given parameters
 */
inline fun <reified T : IProxy<*>> IFacade.registerProxy(proxyBuilder:()->T): T = this.model.registerProxy(proxyBuilder)

/**
 * Register an `ICommand` with the `Controller`.
 *
 * @param noteName
 * the name of the `INotification` to associate the
 * `ICommand` with.
 *
 * @param commandBuilder
 * The builder function to instantiate instance of ICommand class
 */
inline fun <reified T : ICommand> IFacade.registerCommand(noteName: String, commandBuilder:()->T) {
    this.controller.registerCommand(noteName, commandBuilder)
}

/**
 * Retrieve a `IProxy` from the `Model` by name.
 *
 * @return the `IProxy` previously regisetered with the `Model`.
 */
inline fun <reified T : IProxy<*>> IFacade.retrieveProxy(): T = this.model.retrieveProxy()

/**
 * Remove an `IProxy` core from the `Model`
 */
inline fun <reified T : IProxy<*>> IFacade.removeProxy(): T? = this.model.removeProxy() as? T

/**
 * Check if a Proxy is registered.
 *
 * @return whether a Proxy is currently registered
 */
inline fun <reified T : IProxy<*>> IFacade.hasProxy(): Boolean = this.model.hasProxy<T>()


////------------- EXTENSIONS FUNCTION -----////
/**
 * Attach current activity and parent container to this core
 * only one core can has one activity to attach, we cant reattach activity to the core
 *
 * @param activity
 * Current Activity to attach the core
 *
 * @param container
 * Current container (ViewGroup) to add childs viewComponents from Mediators
 */
fun IFacade.attach(activity: AppCompatActivity, container: ViewGroup? = null): IFacade {
    view.attachActivity(activity, container)
    return this
}

/**
 * Hide current mediator by the name and remove it from backstack then show last added mediator at backstack
 * If there is no mediator in backstack there is no action will be (only if bacstack size > 1)
 *
 * @param mediatorName
 * the name of the `IMediator` core to be removed from the screen
 */
fun IFacade.popMediator(mediatorName: String, animation: IAnimator? = null) {
    view.popMediator(mediatorName, animation)
}

/**
 * Hide current mediator by name
 *
 * @param mediatorName
 * the name of the `IMediator` core to be removed from the screen
 *
 * @param popIt
 * Indicates that is need to be removed from backstack
 *
 * @param animation
 * Instance of current animation
 */
fun IFacade.hideMediator(mediatorName: String, popIt: Boolean, animation: IAnimator?) {
    view.hideMediator(mediatorName, popIt, animation)
}

/**
 * Register an Observer for given notification name and callback `INotificator`
 *
 * @param notifName
 * notification name for register with callback
 *
 * @param notificator
 * lambda function (INotification)->Unit
 */
fun IFacade.registerObserver(notifName: String, notificator: INotificator) {
    view.registerObserver(notifName, Observer(notificator, this.multitonKey))
}

/**
 * Register a list of `INotification` interests.
 *
 * @param listNotification
 * array of string notification names
 *
 * @param notificator
 * the callback function
 *
 */
fun IFacade.registerListObservers(listNotification: List<String>, notificator: INotificator): IFacade {
    listNotification.forEach {
        registerObserver(it, notificator)
    }
    return this
}

/**
 * Manually remove observer by given notification name
 *
 * @param notifName
 * notification name for remove with context
 *
 * @param observerContext
 * this is a context for observable compare
 */
fun IFacade.removeObserver(notifName: String, observerContext: Any = this.multitonKey) {
    view.removeObserver(notifName, observerContext)
}

/**
 * Manually remove list of observers by given notification list names
 *
 * @param listNotification
 * array of string notification names
 */
fun IFacade.removeListObservers(listNotification: List<String>) {
    listNotification.forEach {
        removeObserver(it)
    }
}

/**
 * Check if a Command is registered for a given Notification
 *
 * @param notificationName
 * @return whether a Command is currently registered for the given `notificationName`.
 */
fun IFacade.hasCommand(notificationName: String): Boolean = controller.hasCommand(notificationName)

/**
 * Remove a previously registered `ICommand` to `INotification` mapping from the Controller.
 *
 * @param notificationName the name of the `INotification` to remove the `ICommand` mapping for
 */
fun IFacade.removeCommand(notificationName: String) {
    this.controller.removeCommand(notificationName)
}

/**
 * Create and send an `INotification`.
 *
 * <P>
 * Keeps us from having to construct new notification
 * instances in our implementation code.
 * @param notificationName the name of the notification to send
 * @param body the body of the notification (optional)
 * @param type the type of the notification (optional)
</P>
 */
fun IFacade.sendNotification(notificationName: String, body: Any?, type: String?) {
    this.view.notifyObservers(Notification(notificationName, body, type))
}

/**
 * Remove this core by it's `multitonKey`
 */
fun IFacade.remove() {
    IFacade.removeCore(this.multitonKey)
}

/**
 * Manually handle hardware backbutton in Mediator
 *
 * @param animation
 * Current animation for playing when we going to back button press
 *
 * @return Boolean
 * Does current mediator handle back button
 */
fun IFacade.handleBackButton(animation: IAnimator? = LinearAnimator()): Boolean {
    return view.currentShowingMediator?.handleBackButton(animation) ?: false
}