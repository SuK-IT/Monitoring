import {reactive, ref} from 'vue';

const state = reactive({
  expandHost: false,
  expandAgents: false
});

const methods = {

  setExpandHost(newState) {
    if (newState) {
      state.expandHost = true;
    } else {
      state.expandHost = false;
    }
  },

  setExpandAgents(newState) {
    if (newState) {
      state.expandAgents = true;
    } else {
      state.expandAgents = false;
    }
  }

};

export default {
  state,
  methods
};
