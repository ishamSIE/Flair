package com.rasalexman.flaircore.model

import com.rasalexman.flaircore.interfaces.IMapper
import com.rasalexman.flaircore.interfaces.IModel
import com.rasalexman.flaircore.interfaces.IProxy
import com.rasalexman.flaircore.interfaces.instance

/**
 * Created by a.minkin on 21.11.2017.
 */
class Model private constructor(override var multitonKey: String) : IModel {
    /**
     * Mapping of proxyNames to IProxy instances.
     */
    override val proxyMap = HashMap<String, IProxy<*>>()

    companion object : IMapper<Model> {
        override val instanceMap = HashMap<String, Model>()
        /**
         * `Model` Multiton Factory method.
         *
         * @return the core for this Multiton key
         */
        @Synchronized
        fun getInstance(key: String): IModel = instance(key){ Model(key) }

        /**
         * Remove an IModel core
         *
         * @param key of IModel core to remove
         */
        @Synchronized
        fun removeModel(key: String) {
            instanceMap.remove(key)?.clearAll()
        }
    }

    private fun clearAll() {
        proxyMap.clear()
    }
}