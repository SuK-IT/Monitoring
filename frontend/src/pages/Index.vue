<template>
  <q-page>
    <img class="flex absolute-center" alt="SUK-IT logo" src="~assets/logo.webp">
    <q-intersection
      class="intersection"
      once
      transition="scale">
      <q-card>
        <q-card-section class="row flex-center" :style="this.$q.dark.isActive ? 'border-bottom: #C0FFEE 3px solid;' : 'border-bottom: #325358 3px solid;'">
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
            <b>Memory total: </b>{{ hostMemory.total }}<b> Memory free: </b>{{ hostMemory.available }}<b> Memory used: </b>{{ hostMemory.used }}
          </div>
        </q-card-section>
        <q-expansion-item
          dense
          expand-icon-toggle
          expand-separator
          icon="info"
          label="Details"
          :caption="hostHost.host_name"
          header-class="text-center"
          v-model="store.state.expandHost"
        >
          <q-card-section class="row flex-center">
            <div v-for="hostInterface in hostInterfaces" v-bind:key="hostInterface.interface_name">
              <ul>
                <li><b>{{ hostInterface.interface_name }}</b></li>
                <li><b>IP: </b>{{ hostInterface.ip }} / {{ hostInterface.subnet_mask }}</li>
                <li><b>MAC: </b>{{ hostInterface.mac }}</li>
              </ul>
            </div>

            <div v-for="hostDisk in hostDisks" v-bind:key="hostDisk.name">
              <ul>
                <li><b>{{ hostDisk.name }}</b></li>
                <li><b>Total space: </b>{{ hostDisk.size }}</li>
                <li><b>Free space: </b>{{ hostDisk.free }}</li>
                <li><b>Used: </b>{{ hostDisk.used }}</li>
              </ul>
            </div>
          </q-card-section>
        </q-expansion-item>
      </q-card>
    </q-intersection>

    <div v-if="!isAgent">

      <q-intersection
        once
        transition="scale">
        <q-card>
          <q-card-section class="row flex-center">
            <div v-for="agent in agentsInformation" v-bind:key="agent" class="row no-wrap">
              <div v-if="agent.status === 1">
                <q-chip square color="red" text-color="white" icon="report_gmailerrorred" :label="agent.agent">
                  <q-tooltip>
                    {{ agent.message }}
                  </q-tooltip>
                </q-chip>
              </div>
              <div v-else-if="agent.status === 2">
                <q-chip square color="orange" text-color="white" icon="priority_high" :label="agent.agent">
                  <q-tooltip>
                    {{ agent.message }}
                  </q-tooltip>
                </q-chip>
              </div>
            </div>
          </q-card-section>
        </q-card>
      </q-intersection>

    </div>

    <div class="row flex-center" v-if="!isAgent">
        <q-card style="margin: 10px; max-width: 500px;" v-for="agent in agentsInformation" v-bind:key="agent">

          <div v-if="agent.status === 0">
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
                <b>Memory total: </b>{{ agent.memory.total }}<b> Memory free: </b>{{ agent.memory.available }}<b> Memory used: </b>{{ agent.memory.used }}
              </div>
            </q-card-section>

            <q-expansion-item
              dense
              expand-icon-toggle
              expand-separator
              icon="info"
              label="Details"
              :caption="agent.host.host_name"
              header-class="text-center"
              v-model="store.state.expandAgents"
            >

              <q-card-section class="row flex-center">
                <div v-for="agentInterface in agent.host.interfaces" v-bind:key="agentInterface.interface_name">
                  <ul>
                    <li><b>{{ agentInterface.interface_name }}</b></li>
                    <li><b>IP: </b>{{ agentInterface.ip }} / {{ agentInterface.subnet_mask }}</li>
                    <li><b>MAC: </b>{{ agentInterface.mac }}</li>
                  </ul>
                </div>
              </q-card-section>

              <q-card-section class="row flex-center">
                <div v-for="agentDisk in agent.disks" v-bind:key="agentDisk.name">
                  <ul>
                    <li><b>{{ agentDisk.name }}</b></li>
                    <li><b>Total space: </b>{{ agentDisk.size }}</li>
                    <li><b>Free space: </b>{{ agentDisk.free }}</li>
                    <li><b>Used: </b>{{ agentDisk.used }}</li>
                  </ul>
                </div>
              </q-card-section>

            </q-expansion-item>
          </div>

        </q-card>
    </div>

  </q-page>
</template>

<script>
import { host,agents,api } from "boot/axios";
import { defineComponent, inject } from 'vue';
import { ref } from 'vue';

export default defineComponent({
  name: 'PageIndex',
  setup() {

    const store = inject('store');

    let agentsInformation = ref(null);

    return {
      store,
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

    }).catch(error => {

      console.log("Couldn't fetch mode: " + error);

    });

    host.get().then(response => {

      this.hostHost = response.data.host;
      this.hostInterfaces = this.hostHost.interfaces;
      this.hostOs = response.data.os;
      this.hostCpu = response.data.cpu;
      this.hostDisks = response.data.disks;
      this.hostMemory = response.data.memory;

    }).catch(error => {

      console.log("Encountered an error fetching host information: " + error);

    });

    agents.get().then(response => {

      this.agentsInformation = response.data.agents;

    }).catch(error => {

      console.log("Encountered an error fetching host information: " + error);

    });
  }
})
</script>
