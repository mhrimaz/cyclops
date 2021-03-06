/*
 * Copyright (c) 2015. Zuercher Hochschule fuer Angewandte Wissenschaften
 *  All Rights Reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License"); you may
 *     not use this file except in compliance with the License. You may obtain
 *     a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *     WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *     License for the specific language governing permissions and limitations
 *     under the License.
 */
package ch.icclab.cyclops.schedule.runner.openstack;

import ch.icclab.cyclops.consume.data.mapping.openstack.OpenstackEvent;
import ch.icclab.cyclops.consume.data.mapping.openstack.events.OpenstackNeutronEvent;
import ch.icclab.cyclops.consume.data.mapping.usage.OpenStackFloatingIpActiveUsage;
import ch.icclab.cyclops.load.Loader;
import ch.icclab.cyclops.schedule.runner.OpenStackClient;

import java.util.ArrayList;

/**
 * Author: Oleksii Serhiienko
 * Updated on: 26-Aug-16
 * Description: Runner to generate Neutron usage records out of events and send to the queue
 */
public class NeutronUDRRunner extends OpenStackClient {

    @Override
    public String getDbName() {
        return OpenstackNeutronEvent.class.getSimpleName();
    }

    @Override
    public ArrayList<Class> getListOfMeasurements(){
        return new ArrayList<Class>() {{
            add(OpenStackFloatingIpActiveUsage.class);
        }};
    }

    @Override
    public Class getUsageFormat(){
        return OpenstackNeutronEvent.class;
    }

    @Override
    public ArrayList<OpenStackFloatingIpActiveUsage> generateValue(Long eventTime, OpenstackEvent lastEventInScope) {
        OpenstackNeutronEvent transformedEvent = (OpenstackNeutronEvent) lastEventInScope;
        ArrayList<OpenStackFloatingIpActiveUsage> generatedUsages = new ArrayList<>();
        Long eventLastTime = transformedEvent.getTime();
        Long scheduleTime = new Long(Loader.getSettings().getOpenstackSettings().getOpenstackScheduleTime());
        Long currentTime;

        Boolean lastIteration = false;
        do {
            currentTime = eventLastTime + scheduleTime;
            if (currentTime >= eventTime){
                currentTime = eventTime;
                lastIteration = true;
            }
            generatedUsages.add( new OpenStackFloatingIpActiveUsage(
                    currentTime,
                    transformedEvent.getAccount(),
                    transformedEvent.getIp_adress(),
                    transformedEvent.getSource() ,
                    (double) (currentTime - eventLastTime)/1000)
            );
            eventLastTime = currentTime;
        } while (!lastIteration);
        return generatedUsages;
    }

}