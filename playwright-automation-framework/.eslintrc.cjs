/* eslint-env node */
module.exports = {
  // 'plugin:@typescript-eslint/recommended-type-checked'
  extends: [
    "eslint:recommended",
    "plugin:@typescript-eslint/recommended",
    "plugin:@typescript-eslint/stylistic",
    "prettier",
  ],
  parser: "@typescript-eslint/parser",
  // parserOptions: {
  //   project: true,
  //   tsconfigRootDir: __dirname,
  // },
  plugins: ["@typescript-eslint"],
  root: true,
  rules: {
    complexity: ["error", { max: 10 }],
    eqeqeq: ["error", "always"],
    "max-lines": ["error", { max: 200 }],
    "max-params": ["error", { max: 5 }],
    "no-console": 0,
    "no-dupe-args": 0,
    "no-dupe-else-if": 0,
    "no-duplicate-case": 0,
    "no-duplicate-imports": 0,
    "no-fallthrough": ["error", { allowEmptyCase: true }],
    "no-restricted-syntax": [
      "error",
      {
        selector: "CallExpression[callee.property.name='only']",
        message: "We don't want to leave .only on our tests😱",
      },
    ],
    "no-undef": 0,
  },
};
