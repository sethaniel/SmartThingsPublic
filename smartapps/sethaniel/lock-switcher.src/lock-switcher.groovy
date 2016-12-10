/**
 *  Lock Switcher
 *
 *  Copyright 2016 Seth Munroe
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Lock Switcher",
    namespace: "sethaniel",
    author: "Seth Munroe",
    description: "This helper will use a Simulated Switch to monitor status and control of a lock. This is useful so that it can be accessed by devices that do not control locks (such as Google Home).",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("The lock") {
		input(name: "physicalLock", type: "capability.lock", required: true, title: "Select the lock that will be monitored")
        input(name: "notify", type: "bool", required: false, title: "Send notifications when the lock is locked or unlocked?")
        input(name: "simulatedSwitch", type: "capability.switch", required: true, title: "Select the simulated switch that will monitor and control the lock.")
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(physicalLock, "lock.locked", physicalLockHandler)
	subscribe(physicalLock, "lock.unlocked", physicalLockHandler)
	subscribe(physicalLock, "lock.unknown", physicalLockHandler)
	subscribe(physicalLock, "lock.unlocked with timeout", physicalLockHandler)
    
    subscribe(simulatedSwitch, "switch.on", simulatedSwitchHandler)
    subscribe(simulatedSwitch, "switch.off", simulatedSwitchHandler)
}

def physicalLockHandler(evt) {
	logEvent(evt)
    
    if (evt.stringValue == "locked") {
    	if (simulatedSwitch.currentValue("switch") != "on") {
        	simulatedSwitch.on()
        }
    } else { 
        if (simulatedSwitch.currentValue("switch") != "off") {
    		simulatedSwitch.off()
        }
    }
    
    if (notify){
    	sendPush("${evt.stringValue.toUpperCase()}: ${evt.descriptionText}")
    }
}

def simulatedSwitchHandler(evt) {
	logEvent(evt)
    
    if (evt.stringValue == "on") {
    	if (physicalLock.currentValue("lock") != "locked") {
        	physicalLock.lock()
        }
    } else { 
        if (physicalLock.currentValue("lock") != "unlocked"
        && physicalLock.currentValue("lock") != "unlocked with timeout") {
    		physicalLock.unlock()
        }
    }
}

def logEvent(evt) {
	log.debug "event from [${evt.displayName}]"
    log.debug "event.data [$evt.data]"
    log.debug "event.description [$evt.description]"
    log.debug "event.descriptionText [$evt.descriptionText]"
    log.debug "event.value [$evt.value]"
    log.debug "event.stringValue [$evt.stringValue]"
    log.debug "event.digital [$evt.digital]"
    log.debug "event.physical [$evt.physical]"
    log.debug "event.source [$evt.source]"
    try {
    	log.debug "event.jsonValue [$evt.jsonValue]"
    } catch (ex) {
    	log.debug "event.jsonValue [no valid json value]"
    }
}