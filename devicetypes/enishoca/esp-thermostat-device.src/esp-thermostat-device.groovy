/**
 *  ESP Thermostat 
 *
 * 	Author: Enis Hoca  - enishoca@outlook.com
 *  Copyright 2018 Enis Hoca
 *  
 *  Derived from:
 *     Virtual thermostat by @eliotstocker and dht22 redux by geko@statusbits.com
 *      
 * 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */

import groovy.json.JsonSlurper
metadata {
	definition(name: "ESP Thermostat Device", namespace: "enishoca", author: "Enis Hoca") {
		capability "Actuator"
		capability "Refresh"
		capability "Sensor"
		capability "Thermostat"
		//capability "Thermostat Heating Setpoint"
		capability "Thermostat Mode"
		capability "Thermostat Operating State"
		//capability "Thermostat Setpoint"
		capability "Temperature Measurement"
		capability "Health Check"

		command "refresh"
		command "poll"

		command "offbtn"
		command "heatbtn"
		command "setThermostatMode", ["string"]
		command "levelUpDown"
		command "levelUp"
		command "levelDown"
		command "heatingSetpointUp"
		command "heatingSetpointDown"
		command "log"
		command "changeMode"
		command "setVirtualTemperature", ["number"]
		command "setHeatingStatus", ["boolean"]
		command "setEmergencyMode", ["boolean"]
		command "setHeatingOff", ["boolean"]

		attribute "temperatureUnit", "string"
		attribute "targetTemp", "string"
		attribute "debugOn", "string"
		attribute "safetyTempMin", "string"
		attribute "safetyTempMax", "string"
		attribute "safetyTempExceeded", "string"
	}

	simulator {
		// TODO: define status and reply messages here
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "temperature", type: "thermostat", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState("default", label: '${currentValue}°', unit: unitString())
			}
			tileAttribute("device.thermostatSetpoint", key: "VALUE_CONTROL") {
				attributeState("default", action: "levelUpDown")
				attributeState("VALUE_UP", action: "levelUp")
				attributeState("VALUE_DOWN", action: "levelDown")
			}
			tileAttribute("device.thermostatOperatingState", key: "OPERATING_STATE") {
				attributeState("idle", backgroundColor: "#44B621")
				attributeState("heating", backgroundColor: "#FFA81E")
				attributeState("off", backgroundColor: "#ddcccc")
				attributeState("emergency", backgroundColor: "#e60000")
			}
			tileAttribute("device.thermostatMode", key: "THERMOSTAT_MODE") {
				attributeState("off", label: 'Off')
				attributeState("heat", label: 'Heat')
			}
			tileAttribute("device.thermostatSetpoint", key: "HEATING_SETPOINT") {
				attributeState("default", label: '${currentValue}')
			}
		}
		valueTile("temp2", "device.temperature", width: 2, height: 2, decoration: "flat") {
			state("default", label: '${currentValue}°', icon: "https://raw.githubusercontent.com/eliotstocker/SmartThings-VirtualThermostat-WithDTH/master/device.png",
				backgroundColors: getTempColors(), canChangeIcon: true)
		}
		standardTile("thermostatMode", "device.thermostatMode", width: 2, height: 2, decoration: "flat") {
			state("off", action: "changeMode", nextState: "updating", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/off_icon.png")
			state("heat", action: "changeMode", nextState: "updating", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_icon.png")
			state("updating", label: "", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/cmd_working.png")
		}

		standardTile("offBtn", "device.off", width: 1, height: 1, decoration: "flat") {
			state("Off", action: "offbtn", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/off_icon.png")
		}
		standardTile("heatBtn", "device.canHeat", width: 1, height: 1, decoration: "flat") {
			state("Heat", action: "heatbtn", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_icon.png")
			state "false", label: ''
		}
		standardTile("refresh", "device.refresh", width: 2, height: 2, decoration: "flat") {
			state "Refresh", action: "refresh.refresh", icon: "st.secondary.refresh"
		}
		valueTile("heatingSetpoint", "device.thermostatSetpoint", width: 1, height: 1) {
			state("heatingSetpoint", label: '${currentValue}', unit: unitString(), foregroundColor: "#FFFFFF",
				backgroundColors: [
					[value: 0, color: "#FFFFFF"],
					[value: 7, color: "#FF3300"],
					[value: 15, color: "#FF3300"]
				])
			state("disabled", label: '', foregroundColor: "#FFFFFF", backgroundColor: "#FFFFFF")
		}
		standardTile("heatingSetpointUp", "device.thermostatSetpoint", width: 1, height: 1, canChangeIcon: true, decoration: "flat") {
			state "default", label: '', action: "heatingSetpointUp", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_arrow_up.png"
			state "", label: ''
		}
		standardTile("heatingSetpointDown", "device.thermostatSetpoint", width: 1, height: 1, canChangeIcon: true, decoration: "flat") {
			state "default", label: '', action: "heatingSetpointDown", icon: "https://raw.githubusercontent.com/tonesto7/nest-manager/master/Images/Devices/heat_arrow_down.png"
			state "", label: ''
		}
		controlTile("heatSliderControl", "device.thermostatSetpoint", "slider", height: 1, width: 3, range: getRange(), inactiveLabel: false) {
			state "default", action: "setHeatingSetpoint", backgroundColor: "#FF3300"
			state "", label: ''
		}

		main("temp2")
		details(["temperature", "thermostatMode",
			"heatingSetpointDown", "heatingSetpoint", "heatingSetpointUp",
			"heatSliderControl", "offBtn", "heatBtn", "refresh"
		])
	}
	/*preferences {
		input "resetHistoryOnly", "bool", title: "Reset History Data", description: "", displayDuringSetup: false
		input "resetAllData", "bool", title: "Reset All Stored Event Data", description: "", displayDuringSetup: false
	}*/
}

def shouldReportInCentigrade() {
	/*def retVal = true
	try {
    	def ts = getTemperatureScale();
    	retVal = ts == "C"
    } finally { 
		return retVal
    }*/
	return false
}

def installed() {
	log.trace "Executing 'installed'"
	initialize()
	done()
}

def configure() {
	log.trace "Executing 'configure'"
	initialize()
	done()
}

private initialize() {
	log.trace "Executing 'initialize'"

	sendEvent(name: "temperature", value: defaultTemp(), unit: unitString(), displayed: false)
	sendEvent(name: "thermostatSetpoint", value: defaultTemp(), unit: unitString(), displayed: false)
	sendEvent(name: "heatingSetpoint", value: defaultTemp(), unit: unitString(), displayed: false)
	sendEvent(name: "thermostatOperatingState", value: "heating")
	sendEvent(name: "thermostatMode", value: "heat")
}

def getTempColors() {
	def colorMap
	//getTemperatureScale() == "C"   wantMetric()
	if (shouldReportInCentigrade()) {
		colorMap = [
			// Celsius Color Range
			[value: 0, color: "#153591"],
			[value: 7, color: "#1e9cbb"],
			[value: 15, color: "#90d2a7"],
			[value: 23, color: "#44b621"],
			[value: 29, color: "#f1d801"],
			[value: 33, color: "#d04e00"],
			[value: 36, color: "#bc2323"]
		]
	} else {
		colorMap = [
			// Fahrenheit Color Range
			[value: 40, color: "#153591"],
			[value: 44, color: "#1e9cbb"],
			[value: 59, color: "#90d2a7"],
			[value: 74, color: "#44b621"],
			[value: 84, color: "#f1d801"],
			[value: 92, color: "#d04e00"],
			[value: 96, color: "#bc2323"]
		]
	}
}

def unitString() {
	return shouldReportInCentigrade() ? "°C" : "°F"
}
def defaultTemp() {
	return shouldReportInCentigrade() ? 20 : 70
}
def lowRange() {
	return shouldReportInCentigrade() ? 9 : 45
}
def highRange() {
	return shouldReportInCentigrade() ? 32 : 90
}
def getRange() {
	return "(${lowRange()}..${highRange()})"
}

def getTemperature() {
	log.debug " getTemperature"
	def current = device.currentValue("temperature")
	log.debug "curent = ${current}"
	return device.currentValue("temperature")
}

def setHeatingSetpoint(temp) {
	log.debug "setting temp to: $temp"
	sendEvent(name: "thermostatSetpoint", value: temp, unit: unitString())
	sendEvent(name: "heatingSetpoint", value: temp, unit: unitString())
	refresh()
	runIn(10, refresh)
}

def heatingSetpointUp() {
	def hsp = device.currentValue("thermostatSetpoint")
	setHeatingSetpoint(hsp + 1.0)
}

def heatingSetpointDown() {
	def hsp = device.currentValue("thermostatSetpoint")
	setHeatingSetpoint(hsp - 1.0)
}

def levelUp() {
	def hsp = device.currentValue("thermostatSetpoint")
	setHeatingSetpoint(hsp + 1.0)
}

def levelDown() {
	def hsp = device.currentValue("thermostatSetpoint")
	setHeatingSetpoint(hsp - 1.0)
}

private void done() {
	log.trace "---- DONE ----"
}

def ping() {
	log.trace "Executing ping"
	refresh()
}
def parse(data) {
	log.debug "parse data: $data"
}
def refresh() {
	log.trace "Executing refresh"
	sendEvent(name: "thermostatMode", value: getThermostatMode())
	sendEvent(name: "thermostatOperatingState", value: getOperatingState())
	sendEvent(name: "thermostatSetpoint", value: getThermostatSetpoint(), unit: unitString())
	sendEvent(name: "heatingSetpoint", value: getHeatingSetpoint(), unit: unitString())
	sendEvent(name: "temperature", value: getTemperature(), unit: unitString())
	done()
}
def getThermostatMode() {
	return device.currentValue("thermostatMode")
}
def getOperatingState() {
	return device.currentValue("thermostatOperatingState")
}
def getThermostatSetpoint() {
	return device.currentValue("thermostatSetpoint")
}
def getHeatingSetpoint() {
	return device.currentValue("heatingSetpoint")
}
def poll() {
	refresh()
}
def offbtn() {
	log.trace "Offbtn"
	sendEvent(name: "thermostatMode", value: "off")
}
def heatbtn() {
	log.trace "heatbtn"
	sendEvent(name: "thermostatMode", value: "heat")
}
def setThermostatMode(mode) {
	log.trace "setThermostatMode $mode"
	sendEvent(name: "thermostatMode", value: mode)
}
def levelUpDown() {}
def log() {}
def changeMode() {
	def val = device.currentValue("thermostatMode") == "off" ? "heat" : "off"
	sendEvent(name: "thermostatMode", value: val)
	log.trace "changeMode  value: ${val}"
	return val
}
def setVirtualTemperature(temp) {
	log.debug "setVirtualTemperature ${temp}"
	//sendEvent(name:"temperature", value: temp, unit: unitString())
}
def setHeatingStatus(bool) {
	sendEvent(name: "thermostatOperatingState", value: bool ? "heating" : "idle")
}
def setEmergencyMode(bool) {
	sendEvent(name: "thermostatOperatingState", value: bool ? "emergency" : "idle")
}
def setHeatingOff(bool) {
	sendEvent(name: "thermostatOperatingState", value: bool ? "off" : "idle")
}
def parseTstatData(body) {
	def slurper = new JsonSlurper()
	def tstat = slurper.parseText(body)
	def events = []
	if (tstat.containsKey("error_msg")) {
		log.error "Thermostat error: ${tstat.error_msg}"
		return null
	}

	if (tstat.containsKey("success")) {
		// this is POST response - ignore
		return null
	}

	if (tstat.containsKey("temp")) {
		//Float temp = tstat.temp.toFloat()
		def ev = [
			name: "temperature",
			value: scaleTemperature(tstat.temp.toFloat()),
			unit: unitString(),
		]

		sendEvent(ev)
	}

	if (tstat.containsKey("cset")) {
		def ev = [
			name: "coolingSetpoint",
			value: scaleTemperature(tstat.humidity.toFloat()),
			unit: unitString(),
		]
		sendEvent(ev)
	}

	if (tstat.containsKey("hset")) {
		def ev = [
			name: "heatingSetpoint",
			value: scaleTemperature(tstat.heat_index.toFloat()),
			unit: unitString(),
		]
		sendEvent(ev)
		//events << createEvent(ev)
	}

	if (tstat.containsKey("tstate")) {
		def value = parseThermostatState(tstat.tstate)
		if (device.currentState("thermostatOperatingState") ?.value != value) {
			def ev = [
				name: "thermostatOperatingState",
				value: value
			]
			sendEvent(ev)	
		}
	}

	if (tstat.containsKey("fstate")) {
		def value = parseFanState(tstat.fstate)
		if (device.currentState("fanState") ?.value != value) {
			def ev = [
				name: "fanState",
				value: value
			]
			sendEvent(ev)
		}
	}

	if (tstat.containsKey("tmode")) {
		def value = parseThermostatMode(tstat.tmode)
		if (device.currentState("thermostatMode") ?.value != value) {
			def ev = [
				name: "thermostatMode",
				value: value
			]
			log.trace "tstat.containsKey(tmode) ${value}"
			sendEvent(ev)
		}
	}

	if (tstat.containsKey("fmode")) {
		def value = parseFanMode(tstat.fmode)
		if (device.currentState("thermostatFanMode") ?.value != value) {
			def ev = [
				name: "thermostatFanMode",
				value: value
			]
			sendEvent(ev)
		}
	}

	if (tstat.containsKey("hold")) {
		def value = parseThermostatHold(tstat.hold)
		if (device.currentState("hold") ?.value != value) {
			def ev = [
				name: "hold",
				value: value
			]
			sendEvent(ev)
		}
	}

  if (tstat.containsKey("humidity")) {
		def ev = [
			name: "coolingSetpoint",
			value: scaleTemperature(tstat.humidity.toFloat()),
			unit: unitString(),
		]
	}

	if (tstat.containsKey("heat_index")) {
		def ev = [
			name: "heatingSetpoint",
			value: scaleTemperature(tstat.heat_index.toFloat()),
			unit: unitString(),
		]
	}
	return
}
private def scaleTemperature(Float temp) {
	if (getTemperatureScale() == "C") {
		return temperatureFtoC(temp)
	}

	return temp.round(1)
}