<template>
  <q-page>
    <img class="flex absolute-center" alt="SUK-IT logo" src="~assets/logo.webp">
    <q-intersection
      class="intersection"
      once
      transition="scale">
      <q-card>
        <q-card-section class="row flex-center">
          <div>
            <b>Hostname:</b> {{ hostHost.host_name }}
          </div>
          <q-separator style="margin-left: 5px; margin-right: 5px;"/>
          <div>
            <b>Domain name:</b> {{ hostHost.domain_name }}
          </div>
          <q-separator style="margin-left: 5px; margin-right: 5px;"/>
          <div>
            <b>OS manufacturer:</b> {{ hostOs.manufacturer }}
          </div>
          <q-separator style="margin-left: 5px; margin-right: 5px;"/>
          <div>
            <b>OS:</b> {{ hostOs.os }}
          </div>
          <q-separator style="margin-left: 5px; margin-right: 5px;"/>
          <div>
            <b>OS version:</b> {{ hostOs.version }}
          </div>
          <q-separator style="margin-left: 5px; margin-right: 5px;"/>
          <div>
            <b>Uptime:</b> {{ hostOs.uptime }}
          </div>
          <q-separator style="margin-left: 5px; margin-right: 5px;"/>
          <div>
            <b>Disks:</b> {{ hostDisks.length }}
          </div>
          <q-separator style="margin-left: 5px; margin-right: 5px;"/>
          <div>
            <b>Memory total:</b> {{ hostMemory.total }} Memory free: {{ hostMemory.available }}
          </div>
        </q-card-section>
        <q-card-section class="row flex-center">
          <div v-for="hostInterface in hostInterfaces" v-bind:key="hostInterface.interface_name">
            <ul>
              <li><b>{{ hostInterface.interface_name }}</b></li>
              <li><b>IP: </b>{{ hostInterface.ip }} / {{ hostInterface.subnet_mask }}</li>
              <li><b>MAC: </b>{{ hostInterface.mac }}</li>
            </ul>
          </div>
        </q-card-section>
        <q-card-section class="row flex-center">
          <div v-for="hostDisk in hostDisks" v-bind:key="hostDisk.name">
            <ul>
              <li><b>{{ hostDisk.name }}</b></li>
              <li><b>Total space: </b>{{ hostDisk.size }}</li>
              <li><b>Free space: </b>{{ hostDisk.free }}</li>
            </ul>
          </div>
        </q-card-section>
      </q-card>
    </q-intersection>

    <div class="row" v-if="!isAgent">
      <q-card style="margin: 10px; max-width: 500px;" v-for="agent in agentsInformation" v-bind:key="agent">

        <q-card-section class="row flex-center wrap">
          <div>
            <b>Hostname:</b> {{ agent.host.host_name }}
          </div>
          <q-separator style="margin-left: 5px; margin-right: 5px;"/>
          <div>
            <b>Domain name:</b> {{ agent.host.domain_name }}
          </div>
          <q-separator style="margin-left: 5px; margin-right: 5px;"/>
          <div>
            <b>OS manufacturer:</b> {{ agent.os.manufacturer }}
          </div>
          <q-separator style="margin-left: 5px; margin-right: 5px;"/>
          <div>
            <b>OS:</b> {{ agent.os.os }}
          </div>
          <q-separator style="margin-left: 5px; margin-right: 5px;"/>
          <div>
            <b>OS version:</b> {{ agent.os.version }}
          </div>
          <q-separator style="margin-left: 5px; margin-right: 5px;"/>
          <div>
            <b>Uptime:</b> {{ agent.os.uptime }}
          </div>
          <q-separator style="margin-left: 5px; margin-right: 5px;"/>
          <div>
            <b>Disks:</b> {{ agent.disks.length }}
          </div>
          <q-separator style="margin-left: 5px; margin-right: 5px;"/>
          <div>
            <b>Memory total:</b> {{ agent.disks.total }} Memory free: {{ hostMemory.available }}
          </div>
        </q-card-section>

        <q-card-section class="row flex-center">
          <div v-for="hostInterface in hostInterfaces" v-bind:key="hostInterface.interface_name">
            <ul>
              <li><b>{{ hostInterface.interface_name }}</b></li>
              <li><b>IP: </b>{{ hostInterface.ip }} / {{ hostInterface.subnet_mask }}</li>
              <li><b>MAC: </b>{{ hostInterface.mac }}</li>
            </ul>
          </div>
        </q-card-section>

        <q-card-section class="row flex-center">
          <div v-for="hostDisk in hostDisks" v-bind:key="hostDisk.name">
            <ul>
              <li><b>{{ hostDisk.name }}</b></li>
              <li><b>Total space: </b>{{ hostDisk.size }}</li>
              <li><b>Free space: </b>{{ hostDisk.free }}</li>
            </ul>
          </div>
        </q-card-section>

      </q-card>
    </div>
  </q-page>
</template>

<script>
import { host,agents,api } from "boot/axios";
import { defineComponent } from 'vue';
import { ref } from 'vue';

export default defineComponent({
  name: 'PageIndex',
  setup() {

    let agentsInformation = ref(null);

    return {
      agentsInformation,
      hostHost: ref(Object),
      hostInterfaces: ref(Object),
      hostOs: ref(Object),
      hostCpu: ref(Object),
      hostDisks: ref(Object),
      hostMemory: ref(Object),
      agents: ref([100]),
      isAgent: false
    }
  },
  mounted() {
    api.get('/mode').then(response => {
      this.isAgent = response.data.mode;
      console.log(response.data.mode);
  }).catch(error => {
      console.log("Couldn't fetch mode: " + error);
    });

    host.get().then(response => {

      if (response.data.status === 0) {

        console.log("Status host 0");
        this.hostHost = response.data.host;
        this.hostInterfaces = this.hostHost.interfaces;
        this.hostOs = response.data.os;
        this.hostCpu = response.data.cpu;
        this.hostDisks = response.data.disks;
        this.hostMemory = response.data.memory;

      } else if (response.data.status === 1) {

        console.log("Status host 1");

      } else {

        console.log("Couldn't retrieve host information.");

      }

    }).catch(error => {

      console.log("Encountered an error fetching host information: " + error);

    });

    agents.get().then(response => {

      if (response.data.status === 0) {

        console.log("Status agents 0");
        this.agentsInformation = response.data.agents;

      } else if (response.data.status === 1) {

        console.log("Status agents 1");
        this.agentsInformation = response.data.message;

      } else {
        console.log("Couldn't retrieve agents information.");
      }

    }).catch(error => {

      console.log("Encountered an error fetching host information: " + error);

    });
  }
})
</script>
