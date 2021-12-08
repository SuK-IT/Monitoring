import {reactive} from 'vue';

const state = reactive({
  autorefresh: true
});

const methods = {
  setExpandHost(newState) {
    if (newState) {
      state.expandHost = true;
    } else {
      state.expandHost = false;
    }
  }
};

export default {
  state,
  methods
};
