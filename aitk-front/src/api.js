import axios from "axios";
import {Message, Notification} from "element-ui";

export function getModelTreeData(param, successCallback) {
  return axios.post("/aitk/getModelTreeData", param).then(
      response => handleBizError(response, successCallback)).catch(
      handleError);
}

export function handleMessage(param, successCallback) {
  return axios.post("/aitk/handleMessage", param).then(
      response => handleBizError(response, successCallback)).catch(
      handleError);
}

export function getLllModelVOByModelName(param, successCallback) {
  return axios.get("/aitk/getLllModelVOByModelName", {params: param}).then(
      response => handleBizError(response, successCallback)).catch(handleError);
}

export function startLoad(param, successCallback) {
  return axios.post("/aitk/startLoad", param).then(
      response => handleBizError(response, successCallback)).catch(
      handleError);
}

export function getModelStateByModelId(param, successCallback) {
  return axios.post("/aitk/getModelStateByModelId", param).then(
      response => handleBizError(response, successCallback)).catch(
      handleError);
}

export function unloadModel(param, successCallback) {
  return axios.post("/aitk/unloadModel", param).then(
      response => handleBizError(response, successCallback)).catch(
      handleError);
}


export function getModelParamByModelId(param, successCallback) {
  return axios.get("/aitk/getModelParamByModelId", {params: param}).then(
      response => handleBizError(response, successCallback)).catch(handleError);
}

export function getMenuPath(param, successCallback) {
  return axios.get("/aitk/getMenuPath", {params: param}).then(
      response => handleBizError(response, successCallback)).catch(handleError);
}

export function infer(param, successCallback) {
  return axios.post("/aitk/infer", param).then(
      response => handleBizError(response, successCallback)).catch(
      handleError);
}

function handleBizError(response, successCallback) {
  successCallback(response.data);
}

function handleError(error) {
  console.log(error)
  Notification.error({
    title: '服务端错误',
    message: error,
    type: 'error'
  })
}
