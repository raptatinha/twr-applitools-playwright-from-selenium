export default {
  local: {
    prefix: "",
    api: "",
    ui: "",
    suffix: "",
  },
  prod: {
    prefix: "https://admin.",
    base: "",
    api: "https://admin.acme.com/api/",
    ui: "https://admin.acme.com/",
    swagger: "api",
    suffix: ".acme.com",
  },
  test: {
    prefix: "https://admin.",
    base: "acme.",
    suffix: ".acme.com",
    ui: "frontend.",
    ui_parameters: "/?fe-domain=qc&fe-path=",
    api: "/api/",
  },
  stage: {
    prefix: "https://admin.",
    api: "https://admin.stage.acme.com/api/",
    ui: "https://admin.stage.acme.com/",
    suffix: ".stage.acme.com",
  },
};
