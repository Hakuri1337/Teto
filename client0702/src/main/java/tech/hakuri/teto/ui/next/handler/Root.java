package tech.hakuri.teto.ui.next.handler;

import tech.hakuri.teto.feature.impl.render.WebGUI;
import tech.hakuri.teto.ui.next.WebClickGUI;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

/**
 * @author nimo
 */
public class Root implements HttpHandler {
    public static String addCSS(String css, String in) {
        return String.format("<head><meta charset=utf8>%s</head>%s", css, in);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String html = """
                    <body>
                      <h1>幻影</h1>
                    
                      <div>
                        <input type="text" id="configName" placeholder="配置名称" />
                        <button onclick="fetchSave()">保存配置</button>
                        <select id="configList"></select>
                        <button onclick="fetchLoad()">加载配置</button>
                      </div>
                    
                      <button onclick="refreshModules()">刷新模块</button>
                      <div id="modules"></div>
                    
                      <script>
                        const BASE_URL = "http://127.0.0.1:12701";
                    
                        async function refreshModules() {
                          const res = await fetch(`${BASE_URL}/overview`);
                          const categories = await res.json();
                    
                          let result = "";
                          categories.forEach((category) => {
                            result += `<h3>${category.name}</h3>`;
                            category.modules.forEach((module) => {
                              result += `
                                                                <div>
                                                                    <input type="checkbox" onchange="fetchToggle('${
                                                                      module.name
                                                                    }', this.checked)" ${
                                module.enable ? "checked" : ""
                              }>
                                                                    ${module.name}
                                                                    <button onclick="bindKey('${
                                                                      module.name
                                                                    }')">${module.keyName}</button>
                                                                    <br>
                                                                    ${generateValues(module)}
                                                                </div>
                                                            `;
                            });
                          });
                    
                          document.getElementById("modules").innerHTML = result;
                        }
                    
                        function generateValues(module) {
                          return module.values
                            .map((value) => {
                              if (value.type === "boolean") {
                                return `<label>${
                                  value.name
                                }: <input type="checkbox" onchange="fetchUpdate('${
                                  module.name
                                }', '${value.name}', this.checked)" ${
                                  value.enable ? "checked" : ""
                                }></label>`;
                              }
                              if (value.type === "number") {
                                // 使用后端提供的min/max值
                                return `<button onclick="promptNumber('${module.name}', '${value.name}', ${value.numberValue}, ${value.min}, ${value.max})">${value.name}: ${value.numberValue}</button>`;
                              }
                              if (value.type === "modes") {
                                // 动态生成模式选项
                                const modeOptions = value.modes
                                  .map(
                                    (mode) =>
                                      `<option value="${mode}" ${
                                        value.currentMode === mode ? "selected" : ""
                                      }>${mode}</option>`
                                  )
                                  .join("");
                    
                                return `<select onchange="fetchUpdate('${module.name}', '${value.name}', this.value)">${modeOptions}</select>`;
                              }
                            })
                            .join("<br>");
                        }
                    
                        function promptNumber(module, value, current, min, max) {
                          const newValue = prompt(
                            `${value} 当前值: ${current} (范围: ${min}-${max})`,
                            current
                          );
                          if (newValue !== null) {
                            // 确保输入值在有效范围内
                            const clampedValue = Math.max(min, Math.min(max, parseFloat(newValue)));
                            fetchUpdate(module, value, clampedValue);
                          }
                        }
                    
                        function bindKey(module) {
                          const key = prompt("请输入按键:").toUpperCase();
                          if (key)
                            fetch(`${BASE_URL}/bind`, {
                              method: "POST",
                              body: JSON.stringify({ module, key }),
                            });
                          refreshModules();
                        }
                    
                        async function fetchSave() {
                          const name = document.getElementById("configName").value;
                          await fetch(`${BASE_URL}/save`, {
                            method: "POST",
                            body: JSON.stringify({ name }),
                          });
                          refreshConfigList();
                        }
                    
                        async function fetchLoad() {
                          const name = document.getElementById("configList").value;
                          await fetch(`${BASE_URL}/load`, {
                            method: "POST",
                            body: JSON.stringify({ name }),
                          });
                          refreshModules();
                        }
                    
                        function fetchToggle(module, enable) {
                          fetch(`${BASE_URL}/toggle`, {
                            method: "POST",
                            body: JSON.stringify({ module, enable }),
                          });
                          refreshModules();
                        }
                    
                        function fetchUpdate(module, value, newValue) {
                          fetch(`${BASE_URL}/update`, {
                            method: "POST",
                            body: JSON.stringify({ module, value, new: newValue }),
                          });
                          refreshModules();
                        }
                    
                        async function refreshConfigList() {
                          const res = await fetch(`${BASE_URL}/list`);
                          const configs = await res.json();
                          document.getElementById("configList").innerHTML = configs
                            .map((config) => `<option value="${config}">${config}</option>`)
                            .join("");
                        }
                    
                        refreshModules();
                        refreshConfigList();
                      </script>
                    </body>
                    
                    """;
            String modern = """
                    <style>
                        /* 基础样式设置 */
                        :root {
                            --primary-color: #165DFF;
                            --secondary-color: #0E42B3;
                            --background-color: #121212;
                            --card-color: #1E1E1E;
                            --text-color: #E0E0E0;
                            --muted-color: #737373;
                            --border-color: #2C2C2C;
                            --success-color: #00B42A;
                            --error-color: #F53F3F;
                            --warning-color: #FF7D00;
                            --transition: all 0.2s ease;
                        }
                    
                        body {
                            font-family: 'Inter', system-ui, -apple-system, sans-serif;
                            background-color: var(--background-color);
                            color: var(--text-color);
                            margin: 0;
                            padding: 2rem;
                            max-width: 1200px;
                            margin: 0 auto;
                        }
                    
                        h1 {
                            color: var(--primary-color);
                            font-size: 2.5rem;
                            font-weight: 600;
                            margin-bottom: 1.5rem;
                            text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
                        }
                    
                        h3 {
                            color: var(--text-color);
                            font-size: 1.2rem;
                            font-weight: 500;
                            margin-top: 2rem;
                            margin-bottom: 0.75rem;
                            text-transform: capitalize;
                            border-bottom: 1px solid var(--border-color);
                            padding-bottom: 0.5rem;
                        }
                    
                        /* 按钮样式 */
                        button {
                            background-color: var(--primary-color);
                            color: white;
                            border: none;
                            border-radius: 0.375rem;
                            padding: 0.5rem 1rem;
                            font-size: 0.9rem;
                            cursor: pointer;
                            transition: var(--transition);
                            margin: 0.25rem;
                            box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
                        }
                    
                        button:hover {
                            background-color: var(--secondary-color);
                            transform: translateY(-1px);
                            box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
                        }
                    
                        button:active {
                            transform: translateY(0);
                            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
                        }
                    
                        /* 输入框样式 */
                        input[type="text"], select {
                            background-color: var(--card-color);
                            color: var(--text-color);
                            border: 1px solid var(--border-color);
                            border-radius: 0.375rem;
                            padding: 0.5rem 0.75rem;
                            font-size: 0.9rem;
                            margin: 0.25rem;
                            transition: var(--transition);
                        }
                    
                        input[type="text"]:focus, select:focus {
                            outline: none;
                            border-color: var(--primary-color);
                            box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.2);
                        }
                    
                        /* 复选框样式 */
                        input[type="checkbox"] {
                            appearance: none;
                            width: 1.25rem;
                            height: 1.25rem;
                            border: 2px solid var(--border-color);
                            border-radius: 0.25rem;
                            background-color: var(--card-color);
                            cursor: pointer;
                            position: relative;
                            vertical-align: middle;
                            transition: var(--transition);
                        }
                    
                        input[type="checkbox"]:checked {
                            background-color: var(--primary-color);
                            border-color: var(--primary-color);
                        }
                    
                        input[type="checkbox"]:checked::after {
                            content: '✓';
                            position: absolute;
                            top: -1px;
                            left: 3px;
                            color: white;
                            font-size: 1rem;
                        }
                    
                        input[type="checkbox"]:focus {
                            outline: none;
                            box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.2);
                        }
                    
                        /* 模块容器样式 */
                        #modules {
                            display: grid;
                            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
                            gap: 1.5rem;
                            margin-top: 1.5rem;
                        }
                    
                        #modules > div {
                            background-color: var(--card-color);
                            padding: 1rem;
                            border-radius: 0.5rem;
                            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
                            transition: var(--transition);
                            position: relative;
                        }
                    
                        #modules > div:hover {
                            transform: translateY(-2px);
                            box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
                        }
                    
                        #modules > div::before {
                            content: '';
                            position: absolute;
                            top: 0;
                            left: 0;
                            right: 0;
                            height: 3px;
                            background-color: var(--primary-color);
                            border-radius: 0.5rem 0.5rem 0 0;
                        }
                    
                        /* 配置区域样式 */
                        div:first-of-type {
                            background-color: var(--card-color);
                            padding: 1rem;
                            border-radius: 0.5rem;
                            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
                            margin-bottom: 1.5rem;
                        }
                    
                        /* 模式选择器样式 */
                        select {
                            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23E0E0E0'%3E%3Cpath d='M7 10l5 5 5-5z'/%3E%3C/svg%3E");
                            background-repeat: no-repeat;
                            background-position: right 0.5rem center;
                            background-size: 1em;
                            padding-right: 2rem;
                            appearance: none;
                        }
                    
                        /* 加载动画效果 */
                        @keyframes pulse {
                            0%, 100% { opacity: 1; }
                            50% { opacity: 0.5; }
                        }
                    
                        .loading {
                            animation: pulse 1.5s cubic-bezier(0.4, 0, 0.6, 1) infinite;
                        }
                    
                        /* 响应式设计 */
                        @media (max-width: 768px) {
                            body {
                                padding: 1rem;
                            }
                    
                            h1 {
                                font-size: 1.8rem;
                            }
                    
                            #modules {
                                grid-template-columns: 1fr;
                            }
                    
                            div:first-of-type {
                                display: flex;
                                flex-direction: column;
                            }
                    
                            button, input, select {
                                margin: 0.25rem 0;
                            }
                        }
                    </style>
                    """;
            String skeuo = """
                    <style>
                        /* 基础样式设置 */
                        :root {
                            --primary-color: #3498db;
                            --primary-dark: #2980b9;
                            --secondary-color: #e74c3c;
                            --background-color: #f5f5f5;
                            --panel-color: #ecf0f1;
                            --panel-dark: #d5dbdb;
                            --text-color: #2c3e50;
                            --border-color: #bdc3c7;
                            --button-gradient: linear-gradient(to bottom, #3498db, #2980b9);
                            --button-gradient-hover: linear-gradient(to bottom, #2980b9, #2070a0);
                            --switch-on: linear-gradient(to bottom, #2ecc71, #27ae60);
                            --switch-off: linear-gradient(to bottom, #bdc3c7, #95a5a6);
                        }
                    
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background-color: #e0e0e0;
                            background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%239C92AC' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
                            margin: 0;
                            padding: 2rem;
                            max-width: 1200px;
                            margin: 0 auto;
                        }
                    
                        h1 {
                            color: var(--text-color);
                            font-size: 2.5rem;
                            font-weight: 600;
                            margin-bottom: 1.5rem;
                            text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
                            text-align: center;
                        }
                    
                        h3 {
                            color: var(--text-color);
                            font-size: 1.2rem;
                            font-weight: 500;
                            margin-top: 2rem;
                            margin-bottom: 0.75rem;
                            text-transform: capitalize;
                            padding-bottom: 0.5rem;
                            border-bottom: 1px solid var(--border-color);
                            box-shadow: 0 1px 0 rgba(255, 255, 255, 0.5);
                        }
                    
                        /* 按钮样式 - 拟物化设计 */
                        button {
                            background: var(--button-gradient);
                            color: white;
                            border: 1px solid var(--primary-dark);
                            border-radius: 5px;
                            padding: 0.5rem 1rem;
                            font-size: 0.9rem;
                            cursor: pointer;
                            box-shadow:\s
                                0 2px 3px rgba(0, 0, 0, 0.1),
                                inset 0 1px 0 rgba(255, 255, 255, 0.3),
                                inset 0 -1px 0 rgba(0, 0, 0, 0.1);
                            transition: all 0.1s ease;
                            margin: 0.25rem;
                        }
                    
                        button:hover {
                            background: var(--button-gradient-hover);
                            transform: translateY(-1px);
                            box-shadow:\s
                                0 3px 5px rgba(0, 0, 0, 0.15),
                                inset 0 1px 0 rgba(255, 255, 255, 0.3),
                                inset 0 -1px 0 rgba(0, 0, 0, 0.1);
                        }
                    
                        button:active {
                            transform: translateY(1px);
                            box-shadow:\s
                                0 1px 2px rgba(0, 0, 0, 0.1),
                                inset 0 1px 0 rgba(255, 255, 255, 0.1),
                                inset 0 -1px 0 rgba(0, 0, 0, 0.2);
                        }
                    
                        /* 输入框样式 */
                        input[type="text"], select {
                            background-color: white;
                            color: var(--text-color);
                            border: 1px solid var(--border-color);
                            border-radius: 5px;
                            padding: 0.5rem 0.75rem;
                            font-size: 0.9rem;
                            margin: 0.25rem;
                            box-shadow:\s
                                inset 0 1px 3px rgba(0, 0, 0, 0.1);
                            transition: all 0.2s ease;
                        }
                    
                        input[type="text"]:focus, select:focus {
                            outline: none;
                            border-color: var(--primary-color);
                            box-shadow:\s
                                inset 0 1px 3px rgba(0, 0, 0, 0.1),
                                0 0 0 2px rgba(52, 152, 219, 0.2);
                        }
                    
                        /* 复选框样式 - 拟物化开关 */
                        input[type="checkbox"] {
                            appearance: none;
                            width: 50px;
                            height: 26px;
                            border: 1px solid var(--border-color);
                            border-radius: 13px;
                            background: var(--switch-off);
                            cursor: pointer;
                            position: relative;
                            vertical-align: middle;
                            box-shadow:\s
                                inset 0 1px 2px rgba(0, 0, 0, 0.1),
                                0 1px 0 rgba(255, 255, 255, 0.7);
                            transition: all 0.3s ease;
                        }
                    
                        input[type="checkbox"]::after {
                            content: '';
                            position: absolute;
                            top: 2px;
                            left: 2px;
                            width: 20px;
                            height: 20px;
                            background-color: white;
                            border-radius: 50%;
                            box-shadow:\s
                                0 1px 3px rgba(0, 0, 0, 0.3);
                            transition: all 0.3s ease;
                        }
                    
                        input[type="checkbox"]:checked {
                            background: var(--switch-on);
                        }
                    
                        input[type="checkbox"]:checked::after {
                            left: 28px;
                        }
                    
                        input[type="checkbox"]:focus {
                            outline: none;
                            box-shadow:\s
                                inset 0 1px 2px rgba(0, 0, 0, 0.1),
                                0 1px 0 rgba(255, 255, 255, 0.7),
                                0 0 0 2px rgba(52, 152, 219, 0.2);
                        }
                    
                        /* 模块容器样式 - 模拟物理面板 */
                        #modules {
                            display: grid;
                            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
                            gap: 1.5rem;
                            margin-top: 1.5rem;
                        }
                    
                        #modules > div {
                            background-color: var(--panel-color);
                            padding: 1rem;
                            border-radius: 8px;
                            border: 1px solid var(--border-color);
                            box-shadow:\s
                                0 5px 15px rgba(0, 0, 0, 0.1),
                                inset 0 0 0 1px rgba(255, 255, 255, 0.7);
                            position: relative;
                            background-image: linear-gradient(to bottom, rgba(255, 255, 255, 0.8), rgba(255, 255, 255, 0.4));
                        }
                    
                        #modules > div::before {
                            content: '';
                            position: absolute;
                            top: 0;
                            left: 0;
                            right: 0;
                            height: 20px;
                            background-image: linear-gradient(to bottom, rgba(255, 255, 255, 0.6), rgba(255, 255, 255, 0));
                            border-radius: 8px 8px 0 0;
                        }
                    
                        /* 配置区域样式 */
                        div:first-of-type {
                            background-color: var(--panel-color);
                            padding: 1rem;
                            border-radius: 8px;
                            border: 1px solid var(--border-color);
                            box-shadow:\s
                                0 5px 15px rgba(0, 0, 0, 0.1),
                                inset 0 0 0 1px rgba(255, 255, 255, 0.7);
                            margin-bottom: 1.5rem;
                            background-image: linear-gradient(to bottom, rgba(255, 255, 255, 0.8), rgba(255, 255, 255, 0.4));
                        }
                    
                        /* 模式选择器样式 */
                        select {
                            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%232c3e50'%3E%3Cpath d='M7 10l5 5 5-5z'/%3E%3C/svg%3E");
                            background-repeat: no-repeat;
                            background-position: right 0.5rem center;
                            background-size: 1em;
                            padding-right: 2rem;
                            appearance: none;
                        }
                    
                        /* 数字调整按钮 */
                        button:has(+ br + button) {
                            background: linear-gradient(to bottom, #f5f5f5, #e0e0e0);
                            color: var(--text-color);
                            border: 1px solid var(--border-color);
                            padding: 0.3rem 0.7rem;
                        }
                    
                        button:has(+ br + button):hover {
                            background: linear-gradient(to bottom, #e0e0e0, #d0d0d0);
                        }
                    
                        /* 响应式设计 */
                        @media (max-width: 768px) {
                            body {
                                padding: 1rem;
                            }
                    
                            h1 {
                                font-size: 1.8rem;
                            }
                    
                            #modules {
                                grid-template-columns: 1fr;
                            }
                    
                            div:first-of-type {
                                display: flex;
                                flex-direction: column;
                            }
                    
                            button, input, select {
                                margin: 0.25rem 0;
                            }
                        }
                    </style>
                    """;
            String spring = """
                    <style>
                        /* 春节主题 */
                        :root {
                            --primary-color: #e74c3c;    /* 中国红 */
                            --secondary-color: #f1c40f;  /* 金色 */
                            --background-color: #fff8e1; /* 米白色背景 */
                            --text-color: #333;
                            --decoration: #f39c12;       /* 点缀色 */
                        }
                    
                        body {
                            font-family: 'Arial', sans-serif;
                            background-color: var(--background-color);
                            background-image:\s
                                radial-gradient(circle at 10% 20%, rgba(231, 76, 60, 0.05) 0%, transparent 20%),
                                radial-gradient(circle at 90% 80%, rgba(231, 76, 60, 0.05) 0%, transparent 20%);
                            margin: 0;
                            padding: 2rem;
                            max-width: 1200px;
                            margin: 0 auto;
                        }
                    
                        h1 {
                            color: var(--primary-color);
                            font-size: 2.5rem;
                            font-weight: bold;
                            margin-bottom: 1.5rem;
                            text-align: center;
                            text-shadow:\s
                                2px 2px 0 var(--secondary-color),
                                4px 4px 8px rgba(0, 0, 0, 0.2);
                            position: relative;
                        }
                    
                        h1::before, h1::after {
                            content: '✦';
                            color: var(--secondary-color);
                            margin: 0 1rem;
                        }
                    
                        h3 {
                            color: var(--primary-color);
                            font-size: 1.2rem;
                            font-weight: 600;
                            margin-top: 2rem;
                            margin-bottom: 0.75rem;
                            text-transform: capitalize;
                            padding-bottom: 0.5rem;
                            border-bottom: 2px solid var(--secondary-color);
                        }
                    
                        /* 按钮样式 - 红包风格 */
                        button {
                            background: linear-gradient(to bottom, var(--primary-color), #c0392b);
                            color: white;
                            border: none;
                            border-radius: 5px;
                            padding: 0.5rem 1rem;
                            font-size: 0.9rem;
                            cursor: pointer;
                            box-shadow:\s
                                0 4px 0 #a93226,
                                0 6px 12px rgba(0, 0, 0, 0.2);
                            transition: all 0.1s ease;
                            margin: 0.25rem;
                            position: relative;
                            overflow: hidden;
                        }
                    
                        button::after {
                            content: '';
                            position: absolute;
                            top: -50%;
                            left: -50%;
                            width: 200%;
                            height: 200%;
                            background:\s
                                radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
                            transform: scale(0);
                            opacity: 0;
                            transition: transform 0.5s, opacity 0.5s;
                        }
                    
                        button:hover {
                            transform: translateY(-2px);
                            box-shadow:\s
                                0 6px 0 #a93226,
                                0 8px 16px rgba(0, 0, 0, 0.25);
                        }
                    
                        button:active {
                            transform: translateY(2px);
                            box-shadow:\s
                                0 2px 0 #a93226,
                                0 4px 8px rgba(0, 0, 0, 0.2);
                        }
                    
                        button:hover::after {
                            transform: scale(1);
                            opacity: 1;
                        }
                    
                        /* 输入框样式 */
                        input[type="text"], select {
                            background-color: white;
                            color: var(--text-color);
                            border: 2px solid var(--primary-color);
                            border-radius: 5px;
                            padding: 0.5rem 0.75rem;
                            font-size: 0.9rem;
                            margin: 0.25rem;
                            box-shadow:\s
                                inset 0 1px 3px rgba(0, 0, 0, 0.1);
                        }
                    
                        input[type="text"]:focus, select:focus {
                            outline: none;
                            border-color: var(--secondary-color);
                            box-shadow:\s
                                0 0 0 3px rgba(241, 196, 15, 0.3);
                        }
                    
                        /* 复选框样式 - 灯笼风格 */
                        input[type="checkbox"] {
                            appearance: none;
                            width: 24px;
                            height: 24px;
                            border: 2px solid var(--primary-color);
                            border-radius: 50%;
                            background: white;
                            cursor: pointer;
                            position: relative;
                            vertical-align: middle;
                            transition: all 0.3s ease;
                        }
                    
                        input[type="checkbox"]::before {
                            content: '';
                            position: absolute;
                            top: 50%;
                            left: 50%;
                            width: 12px;
                            height: 12px;
                            background: var(--secondary-color);
                            border-radius: 50%;
                            transform: translate(-50%, -50%) scale(0);
                            opacity: 0;
                            transition: all 0.2s ease;
                        }
                    
                        input[type="checkbox"]:checked {
                            background: var(--primary-color);
                            border-color: var(--primary-color);
                        }
                    
                        input[type="checkbox"]:checked::before {
                            transform: translate(-50%, -50%) scale(1);
                            opacity: 1;
                        }
                    
                        /* 模块容器样式 - 春联风格 */
                        #modules {
                            display: grid;
                            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
                            gap: 1.5rem;
                            margin-top: 1.5rem;
                        }
                    
                        #modules > div {
                            background-color: white;
                            padding: 1rem;
                            border-radius: 8px;
                            border: 2px solid var(--primary-color);
                            box-shadow:\s
                                0 5px 15px rgba(0, 0, 0, 0.1);
                            position: relative;
                        }
                    
                        #modules > div::before {
                            content: '';
                            position: absolute;
                            top: 0;
                            left: 0;
                            right: 0;
                            height: 8px;
                            background: var(--secondary-color);
                        }
                    
                        /* 装饰元素 - 鞭炮 */
                        body::before {
                            content: '';
                            position: fixed;
                            top: 0;
                            left: 10px;
                            width: 10px;
                            height: 100%;
                            background: linear-gradient(to bottom, transparent 20%, var(--primary-color) 20%, var(--primary-color) 22%, transparent 22%);
                            background-size: 10px 50px;
                            z-index: -1;
                        }
                    
                        body::after {
                            content: '';
                            position: fixed;
                            top: 0;
                            right: 10px;
                            width: 10px;
                            height: 100%;
                            background: linear-gradient(to bottom, transparent 20%, var(--primary-color) 20%, var(--primary-color) 22%, transparent 22%);
                            background-size: 10px 50px;
                            z-index: -1;
                        }
                    </style>
                    """;
            String dwj = """
                    <style>
                        /* 端午节主题 */
                        :root {
                            --primary-color: #27ae60;    /* 粽叶绿 */
                            --secondary-color: #d35400;  /* 朱砂红 */
                            --background-color: #f8f9fa; /* 淡米色 */
                            --text-color: #333;
                            --decoration: #f1c40f;       /* 点缀色 */
                        }
                    
                        body {
                            font-family: 'Arial', sans-serif;
                            background-color: var(--background-color);
                            background-image:\s
                                url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%2327ae60' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
                            margin: 0;
                            padding: 2rem;
                            max-width: 1200px;
                            margin: 0 auto;
                        }
                    
                        h1 {
                            color: var(--primary-color);
                            font-size: 2.5rem;
                            font-weight: bold;
                            margin-bottom: 1.5rem;
                            text-align: center;
                            text-shadow:\s
                                1px 1px 0 rgba(0, 0, 0, 0.1);
                            position: relative;
                        }
                    
                        h1::before, h1::after {
                            content: '✿';
                            color: var(--secondary-color);
                            margin: 0 1rem;
                        }
                    
                        h3 {
                            color: var(--primary-color);
                            font-size: 1.2rem;
                            font-weight: 600;
                            margin-top: 2rem;
                            margin-bottom: 0.75rem;
                            text-transform: capitalize;
                            padding-bottom: 0.5rem;
                            border-bottom: 2px solid var(--primary-color);
                            position: relative;
                        }
                    
                        h3::after {
                            content: '';
                            position: absolute;
                            bottom: -4px;
                            left: 0;
                            width: 30px;
                            height: 4px;
                            background: var(--secondary-color);
                        }
                    
                        /* 按钮样式 - 粽叶风格 */
                        button {
                            background: linear-gradient(to bottom, var(--primary-color), #219d55);
                            color: white;
                            border: none;
                            border-radius: 5px;
                            padding: 0.5rem 1rem;
                            font-size: 0.9rem;
                            cursor: pointer;
                            box-shadow:\s
                                0 3px 0 #1e8449,
                                0 4px 8px rgba(0, 0, 0, 0.1);
                            transition: all 0.1s ease;
                            margin: 0.25rem;
                            position: relative;
                            overflow: hidden;
                        }
                    
                        button::before {
                            content: '';
                            position: absolute;
                            top: 0;
                            left: -100%;
                            width: 100%;
                            height: 100%;
                            background: linear-gradient(
                                90deg,\s
                                transparent,\s
                                rgba(255, 255, 255, 0.2),\s
                                transparent
                            );
                            transition: all 0.6s ease;
                        }
                    
                        button:hover {
                            transform: translateY(-2px);
                            box-shadow:\s
                                0 5px 0 #1e8449,
                                0 6px 12px rgba(0, 0, 0, 0.15);
                        }
                    
                        button:hover::before {
                            left: 100%;
                        }
                    
                        button:active {
                            transform: translateY(1px);
                            box-shadow:\s
                                0 1px 0 #1e8449,
                                0 2px 4px rgba(0, 0, 0, 0.1);
                        }
                    
                        /* 输入框样式 */
                        input[type="text"], select {
                            background-color: white;
                            color: var(--text-color);
                            border: 2px solid var(--primary-color);
                            border-radius: 5px;
                            padding: 0.5rem 0.75rem;
                            font-size: 0.9rem;
                            margin: 0.25rem;
                            box-shadow:\s
                                inset 0 1px 3px rgba(0, 0, 0, 0.1);
                            transition: all 0.2s ease;
                        }
                    
                        input[type="text"]:focus, select:focus {
                            outline: none;
                            border-color: var(--secondary-color);
                            box-shadow:\s
                                0 0 0 3px rgba(211, 84, 0, 0.2);
                        }
                    
                        /* 复选框样式 - 龙舟风格 */
                        input[type="checkbox"] {
                            appearance: none;
                            width: 26px;
                            height: 26px;
                            border: 2px solid var(--primary-color);
                            border-radius: 4px;
                            background: white;
                            cursor: pointer;
                            position: relative;
                            vertical-align: middle;
                            transition: all 0.3s ease;
                        }
                    
                        input[type="checkbox"]::before {
                            content: '✓';
                            position: absolute;
                            top: 50%;
                            left: 50%;
                            transform: translate(-50%, -50%) scale(0);
                            color: white;
                            font-size: 16px;
                            font-weight: bold;
                            opacity: 0;
                            transition: all 0.2s ease;
                        }
                    
                        input[type="checkbox"]:checked {
                            background: var(--secondary-color);
                            border-color: var(--secondary-color);
                        }
                    
                        input[type="checkbox"]:checked::before {
                            transform: translate(-50%, -50%) scale(1);
                            opacity: 1;
                        }
                    
                        /* 模块容器样式 - 竹节风格 */
                        #modules {
                            display: grid;
                            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
                            gap: 1.5rem;
                            margin-top: 1.5rem;
                        }
                    
                        #modules > div {
                            background-color: white;
                            padding: 1rem;
                            border-radius: 8px;
                            border: 2px solid var(--primary-color);
                            box-shadow:\s
                                0 5px 15px rgba(0, 0, 0, 0.1);
                            position: relative;
                        }
                    
                        #modules > div::before, #modules > div::after {
                            content: '';
                            position: absolute;
                            left: 10px;
                            right: 10px;
                            height: 2px;
                            background: var(--primary-color);
                            opacity: 0.3;
                        }
                    
                        #modules > div::before {
                            top: 25%;
                        }
                    
                        #modules > div::after {
                            bottom: 25%;
                        }
                    </style>
                    """;
            String qrj = """
                    <style>
                        /* 情人节主题 */
                        :root {
                            --primary-color: #e91e63;    /* 浪漫粉 */
                            --secondary-color: #9c27b0;  /* 深紫色 */
                            --background-color: #fff0f5; /* 浅粉色背景 */
                            --text-color: #333;
                            --decoration: #ff80ab;       /* 点缀色 */
                        }
                    
                        body {
                            font-family: 'Arial', sans-serif;
                            background-color: var(--background-color);
                            background-image:\s
                                radial-gradient(circle at 20% 30%, rgba(233, 30, 99, 0.05) 0%, transparent 60%),
                                radial-gradient(circle at 80% 70%, rgba(233, 30, 99, 0.05) 0%, transparent 60%);
                            margin: 0;
                            padding: 2rem;
                            max-width: 1200px;
                            margin: 0 auto;
                        }
                    
                        h1 {
                            color: var(--primary-color);
                            font-size: 2.5rem;
                            font-weight: bold;
                            margin-bottom: 1.5rem;
                            text-align: center;
                            text-shadow:\s
                                2px 2px 4px rgba(0, 0, 0, 0.1);
                            position: relative;
                        }
                    
                        h1::before, h1::after {
                            content: '❤';
                            color: var(--secondary-color);
                            margin: 0 1rem;
                            animation: heartbeat 1.5s ease-in-out infinite;
                        }
                    
                        @keyframes heartbeat {
                            0%, 100% { transform: scale(1); }
                            50% { transform: scale(1.2); }
                        }
                    
                        h3 {
                            color: var(--primary-color);
                            font-size: 1.2rem;
                            font-weight: 600;
                            margin-top: 2rem;
                            margin-bottom: 0.75rem;
                            text-transform: capitalize;
                            padding-bottom: 0.5rem;
                            border-bottom: 2px solid var(--decoration);
                        }
                    
                        /* 按钮样式 - 爱心按钮 */
                        button {
                            background: linear-gradient(to bottom, var(--primary-color), #d81b60);
                            color: white;
                            border: none;
                            border-radius: 20px;
                            padding: 0.5rem 1.2rem;
                            font-size: 0.9rem;
                            cursor: pointer;
                            box-shadow:\s
                                0 4px 8px rgba(233, 30, 99, 0.2);
                            transition: all 0.2s ease;
                            margin: 0.25rem;
                            position: relative;
                            overflow: hidden;
                        }
                    
                        button::before {
                            content: '';
                            position: absolute;
                            top: -50%;
                            left: -50%;
                            width: 200%;
                            height: 200%;
                            background:\s
                                radial-gradient(circle, rgba(255, 255, 255, 0.3) 0%, transparent 70%);
                            transform: scale(0);
                            opacity: 0;
                            transition: transform 0.5s, opacity 0.5s;
                        }
                    
                        button:hover {
                            transform: translateY(-2px);
                            box-shadow:\s
                                0 6px 12px rgba(233, 30, 99, 0.3);
                        }
                    
                        button:hover::before {
                            transform: scale(1);
                            opacity: 1;
                        }
                    
                        button:active {
                            transform: translateY(1px);
                            box-shadow:\s
                                0 2px 4px rgba(233, 30, 99, 0.2);
                        }
                    
                        /* 输入框样式 */
                        input[type="text"], select {
                            background-color: white;
                            color: var(--text-color);
                            border: 2px solid var(--decoration);
                            border-radius: 20px;
                            padding: 0.5rem 0.75rem;
                            font-size: 0.9rem;
                            margin: 0.25rem;
                            box-shadow:\s
                                inset 0 1px 3px rgba(0, 0, 0, 0.1);
                            transition: all 0.2s ease;
                        }
                    
                        input[type="text"]:focus, select:focus {
                            outline: none;
                            border-color: var(--secondary-color);
                            box-shadow:\s
                                0 0 0 3px rgba(156, 39, 176, 0.2);
                        }
                    
                        /* 复选框样式 - 爱心复选框 */
                        input[type="checkbox"] {
                            appearance: none;
                            width: 24px;
                            height: 24px;
                            background: white;
                            cursor: pointer;
                            position: relative;
                            vertical-align: middle;
                            transition: all 0.3s ease;
                        }
                    
                        input[type="checkbox"]::before {
                            content: '';
                            position: absolute;
                            top: 2px;
                            left: 0;
                            width: 16px;
                            height: 26px;
                            background: var(--decoration);
                            border-radius: 50px 50px 0 0;
                            transform: rotate(-45deg);
                            transform-origin: 0 100%;
                            opacity: 0;
                            transition: all 0.2s ease;
                        }
                    
                        input[type="checkbox"]::after {
                            content: '';
                            position: absolute;
                            top: 2px;
                            right: 0;
                            width: 16px;
                            height: 26px;
                            background: var(--decoration);
                            border-radius: 50px 50px 0 0;
                            transform: rotate(45deg);
                            transform-origin: 100% 100%;
                            opacity: 0;
                            transition: all 0.2s ease;
                        }
                    
                        input[type="checkbox"]:checked::before,
                        input[type="checkbox"]:checked::after {
                            opacity: 1;
                        }
                    
                        /* 模块容器样式 - 玫瑰边框 */
                        #modules {
                            display: grid;
                            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
                            gap: 1.5rem;
                            margin-top: 1.5rem;
                        }
                    
                        #modules > div {
                            background-color: white;
                            padding: 1rem;
                            border-radius: 12px;
                            box-shadow:\s
                                0 5px 15px rgba(233, 30, 99, 0.1);
                            position: relative;
                            border: 1px solid rgba(233, 30, 99, 0.2);
                        }
                    
                        #modules > div::before {
                            content: '';
                            position: absolute;
                            top: -10px;
                            right: -10px;
                            width: 20px;
                            height: 20px;
                            background: radial-gradient(circle, var(--primary-color) 0%, transparent 70%);
                            opacity: 0.5;
                        }
                    
                        #modules > div::after {
                            content: '';
                            position: absolute;
                            bottom: -10px;
                            left: -10px;
                            width: 20px;
                            height: 20px;
                            background: radial-gradient(circle, var(--secondary-color) 0%, transparent 70%);
                            opacity: 0.5;
                        }
                    </style>
                    """;
            String xqx = """
                    <style>
                        /* 手绘作文纸风格 */
                        @font-face {
                            font-family: 'Handwriting';
                            src: url('data:font/truetype;charset=utf-8;base64,') format('truetype'); /* 实际项目中应替换为真实字体 */
                            font-display: swap;
                        }
                    
                        :root {
                            --paper-color: #fff9e6;          /* 作文纸颜色 */
                            --line-color: #e6cdaa;           /* 横线颜色 */
                            --margin-color: #d9b38c;         /* 页边距颜色 */
                            --title-color: #333333;          /* 标题颜色 */
                            --text-color: #555555;           /* 文本颜色 */
                            --highlight-color: #ff6b6b;      /* 高亮/强调色 */
                            --staple-color: #8c8c8c;         /* 钉书钉颜色 */
                            --font-family: 'Handwriting', 'Comic Sans MS', cursive, sans-serif; /* 手写风格字体 */
                        }
                    
                        body {
                            font-family: var(--font-family);
                            background-color: #f5f5f0;
                            background-image:\s
                                url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z' fill='%23d9b38c' fill-opacity='0.05' fill-rule='evenodd'/%3E%3C/svg%3E");
                            margin: 0;
                            padding: 2rem;
                            max-width: 900px;
                            margin: 0 auto;
                        }
                    
                        h1 {
                            color: var(--title-color);
                            font-size: 1.8rem;
                            font-weight: bold;
                            margin-bottom: 1.5rem;
                            text-align: center;
                            text-shadow: 1px 1px 1px rgba(0, 0, 0, 0.1);
                            position: relative;
                            padding-bottom: 10px;
                        }
                    
                        h1::after {
                            content: '';
                            position: absolute;
                            bottom: 0;
                            left: 50%;
                            transform: translateX(-50%);
                            width: 80px;
                            height: 2px;
                            background-color: var(--title-color);
                            border-radius: 1px;
                        }
                    
                        h3 {
                            color: var(--title-color);
                            font-size: 1.2rem;
                            font-weight: bold;
                            margin-top: 1.5rem;
                            margin-bottom: 0.75rem;
                            text-transform: capitalize;
                            padding-bottom: 5px;
                            border-bottom: 2px dashed var(--line-color);
                        }
                    
                        /* 作文纸容器 */
                        #modules, div:first-of-type {
                            position: relative;
                            background-color: var(--paper-color);
                            padding: 2rem 2rem 2rem 3rem;
                            border-radius: 3px;
                            box-shadow:\s
                                0 4px 8px rgba(0, 0, 0, 0.1),
                                0 0 0 1px rgba(0, 0, 0, 0.05);
                            margin-bottom: 2rem;
                            background-image:\s
                                linear-gradient(var(--line-color) 1px, transparent 1px);
                            background-size: 100% 25px;
                            background-position: 0 30px;
                        }
                    
                        /* 作文纸页边距 */
                        #modules::before, div:first-of-type::before {
                            content: '';
                            position: absolute;
                            top: 0;
                            left: 25px;
                            bottom: 0;
                            width: 1px;
                            background-color: var(--margin-color);
                            opacity: 0.5;
                        }
                    
                        /* 模拟钉书钉 */
                        #modules::after, div:first-of-type::after {
                            content: '';
                            position: absolute;
                            top: 15px;
                            left: -10px;
                            width: 20px;
                            height: 15px;
                            border: 2px solid var(--staple-color);
                            border-radius: 2px;
                            transform: rotate(-10deg);
                            box-shadow: 0 0 2px rgba(0, 0, 0, 0.2);
                            opacity: 0.7;
                        }
                    
                        /* 按钮样式 - 手绘风格 */
                        button {
                            background: var(--paper-color);
                            color: var(--title-color);
                            border: 2px solid var(--title-color);
                            border-radius: 5px;
                            padding: 0.4rem 0.8rem;
                            font-size: 0.9rem;
                            font-family: var(--font-family);
                            cursor: pointer;
                            box-shadow: 2px 2px 0 var(--title-color);
                            transition: all 0.1s ease;
                            margin: 0.25rem;
                            position: relative;
                            transform: rotate(-1deg);
                        }
                    
                        button:hover {
                            transform: translateY(2px) rotate(-1deg);
                            box-shadow: 1px 1px 0 var(--title-color);
                        }
                    
                        button:active {
                            transform: translateY(4px) rotate(-1deg);
                            box-shadow: 0 0 0 var(--title-color);
                        }
                    
                        /* 特殊按钮样式 */
                        button:has(+ br + button) {
                            transform: rotate(1deg);
                        }
                    
                        button:has(+ br + button):hover {
                            transform: translateY(2px) rotate(1deg);
                        }
                    
                        button:has(+ br + button):active {
                            transform: translateY(4px) rotate(1deg);
                        }
                    
                        /* 输入框样式 */
                        input[type="text"], select {
                            background-color: var(--paper-color);
                            color: var(--text-color);
                            border: 2px solid var(--title-color);
                            border-radius: 5px;
                            padding: 0.4rem 0.7rem;
                            font-size: 0.9rem;
                            font-family: var(--font-family);
                            margin: 0.25rem;
                            box-shadow: inset 1px 1px 0 var(--line-color);
                            transition: all 0.2s ease;
                            transform: rotate(0.5deg);
                        }
                    
                        input[type="text"]:focus, select:focus {
                            outline: none;
                            border-color: var(--highlight-color);
                            transform: rotate(0deg);
                        }
                    
                        /* 复选框样式 */
                        input[type="checkbox"] {
                            appearance: none;
                            width: 18px;
                            height: 18px;
                            border: 2px solid var(--title-color);
                            border-radius: 3px;
                            background-color: var(--paper-color);
                            cursor: pointer;
                            position: relative;
                            vertical-align: middle;
                            transition: all 0.2s ease;
                            transform: rotate(1deg);
                        }
                    
                        input[type="checkbox"]::after {
                            content: '✓';
                            position: absolute;
                            top: 50%;
                            left: 50%;
                            transform: translate(-50%, -50%) scale(0) rotate(-1deg);
                            color: var(--highlight-color);
                            font-size: 16px;
                            font-weight: bold;
                            opacity: 0;
                            transition: all 0.2s ease;
                        }
                    
                        input[type="checkbox"]:checked {
                            border-color: var(--highlight-color);
                        }
                    
                        input[type="checkbox"]:checked::after {
                            transform: translate(-50%, -50%) scale(1) rotate(-1deg);
                            opacity: 1;
                        }
                    
                        /* 模式选择器样式 */
                        select {
                            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23555555'%3E%3Cpath d='M7 10l5 5 5-5z'/%3E%3C/svg%3E");
                            background-repeat: no-repeat;
                            background-position: right 0.5rem center;
                            background-size: 1em;
                            padding-right: 2rem;
                            appearance: none;
                        }
                    
                        /* 手绘装饰元素 */
                        body::before {
                            content: '✎';
                            position: fixed;
                            top: 20px;
                            right: 20px;
                            font-size: 2rem;
                            color: var(--highlight-color);
                            transform: rotate(15deg);
                            opacity: 0.7;
                        }
                    
                        /* 模块容器样式 */
                        #modules > div {
                            margin-bottom: 1.5rem;
                            padding: 0.5rem 0;
                            position: relative;
                        }
                    
                        #modules > div::before {
                            content: '';
                            position: absolute;
                            top: -8px;
                            left: -15px;
                            width: 30px;
                            height: 30px;
                            border-radius: 50%;
                            border: 2px dashed var(--line-color);
                            opacity: 0.5;
                        }
                    </style>
                    """;
            String animal = """
                    <style>
                        /* 卡通动物风格 */
                        :root {
                            --primary-color: #4a90e2;      /* 主色调 - 天空蓝 */
                            --secondary-color: #f5a623;    /* 辅助色 - 暖橙色 */
                            --accent-color: #e34e47;       /* 强调色 - 亮红色 */
                            --background-color: #f8f8f8;   /* 背景色 */
                            --text-color: #333333;         /* 文本色 */
                            --light-text: #666666;         /* 浅色文本 */
                            --card-bg: #ffffff;            /* 卡片背景 */
                            --border-radius: 15px;         /* 圆角半径 */
                        }
                    
                        body {
                            font-family: 'Comic Sans MS', 'Chalkboard SE', sans-serif;
                            background-color: var(--background-color);
                            background-image:\s
                                radial-gradient(circle at 20% 20%, rgba(74, 144, 226, 0.05) 0%, transparent 60%),
                                radial-gradient(circle at 80% 80%, rgba(245, 166, 35, 0.05) 0%, transparent 60%);
                            margin: 0;
                            padding: 2rem;
                            max-width: 1200px;
                            margin: 0 auto;
                        }
                    
                        h1 {
                            color: var(--primary-color);
                            font-size: 2.5rem;
                            font-weight: bold;
                            margin-bottom: 1.5rem;
                            text-align: center;
                            text-shadow:\s
                                2px 2px 0 rgba(0, 0, 0, 0.05),
                                0 0 10px rgba(74, 144, 226, 0.1);
                        }
                    
                        h3 {
                            color: var(--text-color);
                            font-size: 1.5rem;
                            font-weight: bold;
                            margin-top: 2rem;
                            margin-bottom: 1rem;
                            text-transform: capitalize;
                            position: relative;
                            padding-left: 1.5rem;
                        }
                    
                        h3::before {
                            content: '🐾';
                            position: absolute;
                            left: 0;
                            top: -2px;
                            font-size: 1.2rem;
                        }
                    
                        /* 卡通动物按钮 */
                        button {
                            background-color: var(--card-bg);
                            color: var(--text-color);
                            border: 3px solid var(--primary-color);
                            border-radius: 25px;
                            padding: 0.7rem 1.2rem;
                            font-size: 1rem;
                            font-weight: bold;
                            cursor: pointer;
                            box-shadow:\s
                                0 4px 0 var(--primary-color),
                                0 6px 8px rgba(0, 0, 0, 0.1);
                            transition: all 0.2s ease;
                            margin: 0.5rem;
                            position: relative;
                            overflow: hidden;
                        }
                    
                        button:hover {
                            transform: translateY(-3px);
                            box-shadow:\s
                                0 7px 0 var(--primary-color),
                                0 9px 12px rgba(0, 0, 0, 0.15);
                        }
                    
                        button:active {
                            transform: translateY(2px);
                            box-shadow:\s
                                0 2px 0 var(--primary-color),
                                0 3px 4px rgba(0, 0, 0, 0.1);
                        }
                    
                        /* 特殊动物主题按钮 */
                        button:has(+ br + button) {
                            border-color: var(--secondary-color);
                            box-shadow:\s
                                0 4px 0 var(--secondary-color),
                                0 6px 8px rgba(0, 0, 0, 0.1);
                        }
                    
                        button:has(+ br + button):hover {
                            box-shadow:\s
                                0 7px 0 var(--secondary-color),
                                0 9px 12px rgba(0, 0, 0, 0.15);
                        }
                    
                        button:has(+ br + button):active {
                            box-shadow:\s
                                0 2px 0 var(--secondary-color),
                                0 3px 4px rgba(0, 0, 0, 0.1);
                        }
                    
                        /* 按钮装饰元素 */
                        button::before {
                            content: '';
                            position: absolute;
                            top: -10px;
                            right: -10px;
                            width: 30px;
                            height: 30px;
                            background-color: var(--primary-color);
                            border-radius: 50%;
                            opacity: 0.1;
                        }
                    
                        button:has(+ br + button)::before {
                            background-color: var(--secondary-color);
                        }
                    
                        /* 输入框样式 */
                        input[type="text"], select {
                            background-color: var(--card-bg);
                            color: var(--text-color);
                            border: 2px solid var(--primary-color);
                            border-radius: 15px;
                            padding: 0.7rem 1rem;
                            font-size: 1rem;
                            margin: 0.5rem;
                            box-shadow:\s
                                inset 0 2px 4px rgba(0, 0, 0, 0.05);
                            transition: all 0.2s ease;
                        }
                    
                        input[type="text"]:focus, select:focus {
                            outline: none;
                            border-color: var(--secondary-color);
                            box-shadow:\s
                                0 0 0 3px rgba(245, 166, 35, 0.2);
                        }
                    
                        /* 复选框样式 - 动物脚印 */
                        input[type="checkbox"] {
                            appearance: none;
                            width: 28px;
                            height: 28px;
                            background-color: var(--card-bg);
                            border: 2px solid var(--primary-color);
                            border-radius: 8px;
                            cursor: pointer;
                            position: relative;
                            vertical-align: middle;
                            transition: all 0.2s ease;
                        }
                    
                        input[type="checkbox"]::after {
                            content: '🐾';
                            position: absolute;
                            top: 50%;
                            left: 50%;
                            transform: translate(-50%, -50%) scale(0);
                            font-size: 1.2rem;
                            opacity: 0;
                            transition: all 0.2s ease;
                        }
                    
                        input[type="checkbox"]:checked {
                            background-color: var(--primary-color);
                            border-color: var(--primary-color);
                        }
                    
                        input[type="checkbox"]:checked::after {
                            transform: translate(-50%, -50%) scale(1);
                            opacity: 1;
                        }
                    
                        /* 模块容器样式 */
                        #modules {
                            display: grid;
                            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
                            gap: 1.5rem;
                            margin-top: 1.5rem;
                        }
                    
                        #modules > div {
                            background-color: var(--card-bg);
                            padding: 1.5rem;
                            border-radius: var(--border-radius);
                            border: 2px solid var(--primary-color);
                            box-shadow:\s
                                0 5px 15px rgba(0, 0, 0, 0.05);
                            transition: all 0.2s ease;
                            position: relative;
                            overflow: hidden;
                        }
                    
                        #modules > div:hover {
                            transform: translateY(-5px);
                            box-shadow:\s
                                0 10px 20px rgba(0, 0, 0, 0.1);
                        }
                    
                        /* 容器装饰元素 */
                        #modules > div::before {
                            content: '';
                            position: absolute;
                            top: -20px;
                            right: -20px;
                            width: 60px;
                            height: 60px;
                            background-color: var(--secondary-color);
                            border-radius: 50%;
                            opacity: 0.1;
                        }
                    
                        #modules > div::after {
                            content: '';
                            position: absolute;
                            bottom: -15px;
                            left: -15px;
                            width: 40px;
                            height: 40px;
                            background-color: var(--primary-color);
                            border-radius: 50%;
                            opacity: 0.1;
                        }
                    
                        /* 配置区域样式 */
                        div:first-of-type {
                            background-color: var(--card-bg);
                            padding: 1.5rem;
                            border-radius: var(--border-radius);
                            border: 2px solid var(--primary-color);
                            box-shadow:\s
                                0 5px 15px rgba(0, 0, 0, 0.05);
                            margin-bottom: 1.5rem;
                            position: relative;
                        }
                    
                        /* 模式选择器样式 */
                        select {
                            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23666666'%3E%3Cpath d='M7 10l5 5 5-5z'/%3E%3C/svg%3E");
                            background-repeat: no-repeat;
                            background-position: right 0.8rem center;
                            background-size: 1em;
                            padding-right: 2.5rem;
                            appearance: none;
                        }
                    
                        /* 装饰元素 */
                        h1::before {
                            content: '🦊';
                            margin-right: 0.5rem;
                        }
                    
                        h1::after {
                            content: '🐼';
                            margin-left: 0.5rem;
                        }
                    </style>
                    """;
            String pokemon = """
                    <style>
                        /* 神奇宝贝风格 */
                        :root {
                            --primary-color: #FFDE00;      /* 宝可梦黄 */
                            --secondary-color: #CC0000;    /* 宝可梦红 */
                            --accent-color: #3B4CCA;       /* 宝可梦蓝 */
                            --dark-color: #2D2727;         /* 深色文本 */
                            --light-color: #F5F5F5;        /* 浅色背景 */
                            --card-bg: #FFFFFF;            /* 卡片背景 */
                            --border-radius: 8px;          /* 圆角半径 */
                            --pixel-size: 2px;             /* 像素大小 */
                        }
                    
                        body {
                            font-family: 'Press Start 2P', 'Courier New', monospace;
                            background-color: var(--light-color);
                            background-image:\s
                                linear-gradient(45deg, rgba(255, 222, 0, 0.05) 25%, transparent 25%),
                                linear-gradient(-45deg, rgba(255, 222, 0, 0.05) 25%, transparent 25%),
                                linear-gradient(45deg, transparent 75%, rgba(255, 222, 0, 0.05) 75%),
                                linear-gradient(-45deg, transparent 75%, rgba(255, 222, 0, 0.05) 75%);
                            background-size: 50px 50px;
                            background-position: 0 0, 0 25px, 25px -25px, -25px 0px;
                            margin: 0;
                            padding: 2rem;
                            max-width: 1200px;
                            margin: 0 auto;
                        }
                    
                        h1 {
                            color: var(--secondary-color);
                            font-size: 1.8rem;
                            font-weight: bold;
                            margin-bottom: 1.5rem;
                            text-align: center;
                            text-shadow:\s
                                3px 3px 0 var(--primary-color),
                                5px 5px 0 var(--dark-color);
                            letter-spacing: 2px;
                        }
                    
                        h3 {
                            color: var(--dark-color);
                            font-size: 1rem;
                            font-weight: bold;
                            margin-top: 1.5rem;
                            margin-bottom: 1rem;
                            text-transform: uppercase;
                            position: relative;
                            padding-left: 2rem;
                        }
                    
                        h3::before {
                            content: '●';
                            position: absolute;
                            left: 0;
                            top: 0;
                            color: var(--primary-color);
                            text-shadow: 1px 1px 0 var(--dark-color);
                        }
                    
                        /* 像素化按钮 - 宝可梦风格 */
                        button {
                            background-color: var(--primary-color);
                            color: var(--dark-color);
                            border: var(--pixel-size) solid var(--dark-color);
                            border-radius: 0;
                            padding: 0.7rem 1.2rem;
                            font-size: 0.8rem;
                            font-family: 'Press Start 2P', 'Courier New', monospace;
                            cursor: pointer;
                            box-shadow:\s
                                var(--pixel-size) var(--pixel-size) 0 var(--dark-color);
                            transition: all 0.1s ease;
                            margin: 0.5rem;
                            position: relative;
                            overflow: hidden;
                        }
                    
                        button:hover {
                            background-color: #FFEE58;
                            transform: translate(var(--pixel-size), var(--pixel-size));
                            box-shadow: 0 0 0 var(--dark-color);
                        }
                    
                        button:active {
                            background-color: #F5D76E;
                            transform: translate(calc(var(--pixel-size) * 2), calc(var(--pixel-size) * 2));
                        }
                    
                        /* 特殊按钮样式 */
                        button:has(+ br + button) {
                            background-color: var(--secondary-color);
                            color: var(--light-color);
                        }
                    
                        button:has(+ br + button):hover {
                            background-color: #E53935;
                        }
                    
                        button:has(+ br + button):active {
                            background-color: #C62828;
                        }
                    
                        /* 输入框样式 */
                        input[type="text"], select {
                            background-color: var(--card-bg);
                            color: var(--dark-color);
                            border: var(--pixel-size) solid var(--dark-color);
                            border-radius: 0;
                            padding: 0.7rem 1rem;
                            font-size: 0.8rem;
                            font-family: 'Press Start 2P', 'Courier New', monospace;
                            margin: 0.5rem;
                            box-shadow:\s
                                inset var(--pixel-size) var(--pixel-size) 0 rgba(0, 0, 0, 0.1);
                            transition: all 0.2s ease;
                        }
                    
                        input[type="text"]:focus, select:focus {
                            outline: none;
                            border-color: var(--accent-color);
                            box-shadow:\s
                                inset var(--pixel-size) var(--pixel-size) 0 rgba(0, 0, 0, 0.1),
                                0 0 0 var(--pixel-size) var(--accent-color);
                        }
                    
                        /* 复选框样式 - 宝可梦球 */
                        input[type="checkbox"] {
                            appearance: none;
                            width: 24px;
                            height: 24px;
                            background: linear-gradient(
                                to bottom,\s
                                var(--secondary-color) 45%,\s
                                var(--dark-color) 45%,\s
                                var(--dark-color) 55%,\s
                                var(--light-color) 55%
                            );
                            border: var(--pixel-size) solid var(--dark-color);
                            border-radius: 50%;
                            cursor: pointer;
                            position: relative;
                            vertical-align: middle;
                            transition: all 0.2s ease;
                        }
                    
                        input[type="checkbox"]::after {
                            content: '';
                            position: absolute;
                            top: 50%;
                            left: 50%;
                            transform: translate(-50%, -50%);
                            width: 8px;
                            height: 8px;
                            background-color: var(--light-color);
                            border: var(--pixel-size) solid var(--dark-color);
                            border-radius: 50%;
                        }
                    
                        input[type="checkbox"]:checked::before {
                            content: '';
                            position: absolute;
                            top: 50%;
                            left: 50%;
                            transform: translate(-50%, -50%);
                            width: 4px;
                            height: 4px;
                            background-color: var(--primary-color);
                            border-radius: 50%;
                            z-index: 1;
                        }
                    
                        /* 模块容器样式 - 宝可梦卡片 */
                        #modules {
                            display: grid;
                            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
                            gap: 1.5rem;
                            margin-top: 1.5rem;
                        }
                    
                        #modules > div {
                            background-color: var(--card-bg);
                            padding: 1rem;
                            border: var(--pixel-size) solid var(--dark-color);
                            border-radius: var(--border-radius);
                            box-shadow:\s
                                4px 4px 0 rgba(0, 0, 0, 0.1);
                            transition: all 0.2s ease;
                            position: relative;
                            overflow: hidden;
                            background-image:\s
                                linear-gradient(45deg, rgba(0, 0, 0, 0.03) 25%, transparent 25%),
                                linear-gradient(-45deg, rgba(0, 0, 0, 0.03) 25%, transparent 25%),
                                linear-gradient(45deg, transparent 75%, rgba(0, 0, 0, 0.03) 75%),
                                linear-gradient(-45deg, transparent 75%, rgba(0, 0, 0, 0.03) 75%);
                            background-size: 20px 20px;
                        }
                    
                        #modules > div:hover {
                            transform: translateY(-5px);
                            box-shadow:\s
                                6px 6px 0 rgba(0, 0, 0, 0.15);
                        }
                    
                        /* 容器顶部装饰 */
                        #modules > div::before {
                            content: '';
                            position: absolute;
                            top: 0;
                            left: 0;
                            right: 0;
                            height: 4px;
                            background-color: var(--primary-color);
                        }
                    
                        /* 配置区域样式 */
                        div:first-of-type {
                            background-color: var(--card-bg);
                            padding: 1.5rem;
                            border: var(--pixel-size) solid var(--dark-color);
                            border-radius: var(--border-radius);
                            box-shadow:\s
                                4px 4px 0 rgba(0, 0, 0, 0.1);
                            margin-bottom: 1.5rem;
                            position: relative;
                        }
                    
                        /* 模式选择器样式 */
                        select {
                            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%232D2727'%3E%3Cpath d='M7 10l5 5 5-5z'/%3E%3C/svg%3E");
                            background-repeat: no-repeat;
                            background-position: right 0.8rem center;
                            background-size: 1em;
                            padding-right: 2.5rem;
                            appearance: none;
                        }
                    
                        /* 装饰元素 */
                        h1::before, h1::after {
                            content: '●';
                            color: var(--primary-color);
                            text-shadow: 1px 1px 0 var(--dark-color);
                            margin: 0 0.5rem;
                        }
                    </style>
                    """;
            String wx = """
                    <style>
                        /* 文艺手绘风格UI */
                        :root {
                            --primary-color: #8D6E63;      /* 暖棕色 */
                            --secondary-color: #A5D6A7;    /* 淡绿色 */
                            --accent-color: #FF8A65;       /* 珊瑚色 */
                            --light-color: #FFF8E1;        /* 米白色 */
                            --text-color: #4E342E;         /* 深棕色文本 */
                            --border-radius: 12px;         /* 圆角 */
                        }
                    
                        body {
                            font-family: 'Comic Sans MS', 'Chalkboard SE', cursive;
                            background-color: var(--light-color);
                            background-image:\s
                                url("data:image/svg+xml,%3Csvg width='100' height='100' viewBox='0 0 100 100' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M11 18c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm48 25c3.866 0 7-3.134 7-7s-3.134-7-7-7-7 3.134-7 7 3.134 7 7 7zm-43-7c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm63 31c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM34 90c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zm56-76c1.657 0 3-1.343 3-3s-1.343-3-3-3-3 1.343-3 3 1.343 3 3 3zM12 86c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm28-65c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm23-11c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-6 60c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm29 22c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zM32 63c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm57-13c2.76 0 5-2.24 5-5s-2.24-5-5-5-5 2.24-5 5 2.24 5 5 5zm-9-21c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM60 91c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM35 41c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2zM12 60c1.105 0 2-.895 2-2s-.895-2-2-2-2 .895-2 2 .895 2 2 2z' fill='%23d9b38c' fill-opacity='0.05' fill-rule='evenodd'/%3E%3C/svg%3E");
                            margin: 0;
                            padding: 2rem;
                            max-width: 1200px;
                            margin: 0 auto;
                        }
                    
                        h1 {
                            color: var(--primary-color);
                            font-size: 2.5rem;
                            font-weight: bold;
                            margin-bottom: 2rem;
                            text-align: center;
                            text-shadow: 1px 1px 1px rgba(0, 0, 0, 0.1);
                            position: relative;
                        }
                    
                        h1::before,
                        h1::after {
                            content: '🌸';
                            margin: 0 1rem;
                        }
                    
                        h3 {
                            color: var(--primary-color);
                            font-size: 1.5rem;
                            font-weight: bold;
                            margin-top: 2rem;
                            margin-bottom: 1rem;
                            text-transform: capitalize;
                            position: relative;
                            padding-left: 2rem;
                        }
                    
                        h3::before {
                            content: '🌿';
                            position: absolute;
                            left: 0;
                            top: -2px;
                        }
                    
                        /* 藤蔓装饰 */
                        .vine-decoration {
                            position: fixed;
                            top: 0;
                            left: 0;
                            width: 100%;
                            height: 100%;
                            pointer-events: none;
                            z-index: -1;
                            opacity: 0.6;
                        }
                    
                        .vine-decoration::before {
                            content: '';
                            position: absolute;
                            top: 0;
                            left: 0;
                            width: 15px;
                            height: 100%;
                            background-image:\s
                                linear-gradient(to bottom, transparent 10%, var(--secondary-color) 10%, var(--secondary-color) 15%, transparent 15%,\s
                                                transparent 30%, var(--secondary-color) 30%, var(--secondary-color) 35%, transparent 35%,
                                                transparent 50%, var(--secondary-color) 50%, var(--secondary-color) 55%, transparent 55%,
                                                transparent 70%, var(--secondary-color) 70%, var(--secondary-color) 75%, transparent 75%,
                                                transparent 90%, var(--secondary-color) 90%, var(--secondary-color) 95%, transparent 95%);
                            background-size: 100% 100px;
                        }
                    
                        .vine-decoration::after {
                            content: '';
                            position: absolute;
                            top: 0;
                            right: 0;
                            width: 15px;
                            height: 100%;
                            background-image:\s
                                linear-gradient(to bottom, transparent 20%, var(--secondary-color) 20%, var(--secondary-color) 25%, transparent 25%,\s
                                                transparent 40%, var(--secondary-color) 40%, var(--secondary-color) 45%, transparent 45%,
                                                transparent 60%, var(--secondary-color) 60%, var(--secondary-color) 65%, transparent 65%,
                                                transparent 80%, var(--secondary-color) 80%, var(--secondary-color) 85%, transparent 85%,
                                                transparent 95%, var(--secondary-color) 95%, var(--secondary-color) 100%);
                            background-size: 100% 100px;
                        }
                    
                        /* 阿狸风格按钮 */
                        .button-ali {
                            background-color: var(--accent-color);
                            color: white;
                            border: none;
                            border-radius: 25px;
                            padding: 0.7rem 1.5rem;
                            font-size: 1rem;
                            font-weight: bold;
                            cursor: pointer;
                            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                            transition: all 0.2s ease;
                            margin: 0.5rem;
                            position: relative;
                            overflow: hidden;
                        }
                    
                        .button-ali:hover {
                            transform: translateY(-2px);
                            box-shadow: 0 6px 10px rgba(0, 0, 0, 0.15);
                        }
                    
                        .button-ali:active {
                            transform: translateY(1px);
                            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                        }
                    
                        .button-ali::before {
                            content: '';
                            position: absolute;
                            top: -10px;
                            left: -10px;
                            width: 30px;
                            height: 30px;
                            background-color: white;
                            border-radius: 50%;
                            opacity: 0.2;
                            transform: scale(0);
                            transition: transform 0.3s ease;
                        }
                    
                        .button-ali:hover::before {
                            transform: scale(1);
                        }
                    
                        /* 输入框样式 */
                        input[type="text"], select {
                            background-color: rgba(255, 255, 255, 0.7);
                            color: var(--text-color);
                            border: 2px solid var(--secondary-color);
                            border-radius: 15px;
                            padding: 0.7rem 1rem;
                            font-size: 1rem;
                            margin: 0.5rem;
                            box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
                            transition: all 0.2s ease;
                        }
                    
                        input[type="text"]:focus, select:focus {
                            outline: none;
                            border-color: var(--accent-color);
                            box-shadow: 0 0 0 3px rgba(255, 138, 101, 0.2);
                        }
                    
                        /* 复选框样式 - 花朵 */
                        input[type="checkbox"] {
                            appearance: none;
                            width: 24px;
                            height: 24px;
                            background-color: rgba(255, 255, 255, 0.7);
                            border: 2px solid var(--secondary-color);
                            border-radius: 50%;
                            cursor: pointer;
                            position: relative;
                            vertical-align: middle;
                            transition: all 0.2s ease;
                        }
                    
                        input[type="checkbox"]::after {
                            content: '🌸';
                            position: absolute;
                            top: 50%;
                            left: 50%;
                            transform: translate(-50%, -50%) scale(0);
                            font-size: 1rem;
                            opacity: 0;
                            transition: all 0.2s ease;
                        }
                    
                        input[type="checkbox"]:checked {
                            background-color: var(--accent-color);
                            border-color: var(--accent-color);
                        }
                    
                        input[type="checkbox"]:checked::after {
                            transform: translate(-50%, -50%) scale(1);
                            opacity: 1;
                        }
                    
                        /* 模块容器样式 */
                        .module {
                            background-color: rgba(255, 255, 255, 0.7);
                            padding: 1.5rem;
                            border-radius: var(--border-radius);
                            border: 1px solid rgba(141, 110, 99, 0.2);
                            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
                            margin-bottom: 1.5rem;
                            position: relative;
                            backdrop-filter: blur(5px);
                        }
                    
                        /* 装饰性阿狸元素 */
                        .ali-decoration {
                            position: fixed;
                            bottom: 20px;
                            right: 20px;
                            width: 80px;
                            height: 80px;
                            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Ccircle cx='50' cy='40' r='20' fill='%23FF8A65'/%3E%3Ccircle cx='35' cy='30' r='5' fill='white'/%3E%3Ccircle cx='65' cy='30' r='5' fill='white'/%3E%3Cpath d='M35 40 Q40 60 50 65 Q60 60 65 40' fill='none' stroke='%23FF8A65' stroke-width='3'/%3E%3Cpath d='M30 40 L20 30 L10 40' fill='%23FF8A65'/%3E%3Cpath d='M70 40 L80 30 L90 40' fill='%23FF8A65'/%3E%3C/svg%3E");
                            background-size: contain;
                            background-repeat: no-repeat;
                            opacity: 0.8;
                            z-index: -1;
                        }
                    
                        /* 装饰性花草元素 */
                        .flower-decoration {
                            position: absolute;
                            width: 20px;
                            height: 20px;
                            background-color: var(--accent-color);
                            border-radius: 50%;
                            opacity: 0.7;
                        }
                    
                        .flower-decoration::before,
                        .flower-decoration::after {
                            content: '';
                            position: absolute;
                            width: 20px;
                            height: 20px;
                            background-color: var(--accent-color);
                            border-radius: 50%;
                        }
                    
                        .flower-decoration::before {
                            transform: translateX(-15px);
                        }
                    
                        .flower-decoration::after {
                            transform: translateX(15px);
                        }
                    
                        .flower-decoration:nth-child(even) {
                            background-color: var(--secondary-color);
                        }
                    
                        .flower-decoration:nth-child(even)::before,
                        .flower-decoration:nth-child(even)::after {
                            background-color: var(--secondary-color);
                        }
                    </style>
                    """;
            String wood = """
                    <style>
                      :root {
                        --wood-light: #e8d4b5;
                        --wood-medium: #c19a6b;
                        --wood-dark: #8b5a2b;
                        --wood-accent: #603813;
                        --wood-texture: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='4' height='4' viewBox='0 0 4 4'%3E%3Cpath fill='%238b5a2b' fill-opacity='0.1' d='M1 3h1v1H1V3zm2-2h1v1H3V1z'%3E%3C/path%3E%3C/svg%3E");
                        --wood-grain: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='100' height='100' viewBox='0 0 100 100'%3E%3Cpath d='M0 0h100v100H0z' fill='none'/%3E%3Cpath d='M0 0c20 0 20 10 40 10s20-10 40-10c20 0 20 10 40 10s20-10 40-10v10c-20 0-20 10-40 10s-20-10-40-10c-20 0-20 10-40 10S0 20 0 20V0z' fill='%238b5a2b' fill-opacity='0.05'/%3E%3C/svg%3E");
                      }
                    
                      body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        margin: 0;
                        padding: 2rem;
                        background-color: var(--wood-light);
                        background-image: var(--wood-texture), var(--wood-grain);
                        color: var(--wood-accent);
                        position: relative;
                      }
                    
                      /* 木质纹理效果 */
                      body::before {
                        content: "";
                        position: fixed;
                        top: 0;
                        left: 0;
                        width: 100%;
                        height: 100%;
                        background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='100' height='100' viewBox='0 0 100 100'%3E%3Cpath d='M0 0c20 0 20 10 40 10s20-10 40-10c20 0 20 10 40 10s20-10 40-10v10c-20 0-20 10-40 10s-20-10-40-10c-20 0-20 10-40 10S0 20 0 20V0z' fill='%238b5a2b' fill-opacity='0.03'/%3E%3C/svg%3E");
                        pointer-events: none;
                        z-index: -1;
                      }
                    
                      h1 {
                        text-align: center;
                        margin-bottom: 2rem;
                        font-size: 2.5rem;
                        font-weight: 300;
                        color: var(--wood-accent);
                        text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1);
                        position: relative;
                      }
                    
                      /* 木质标题装饰 */
                      h1::after {
                        content: "";
                        position: absolute;
                        bottom: -15px;
                        left: 50%;
                        transform: translateX(-50%);
                        width: 120px;
                        height: 3px;
                        background: linear-gradient(90deg, rgba(120, 81, 16, 0) 0%, rgba(120, 81, 16, 0.7) 50%, rgba(120, 81, 16, 0) 100%);
                      }
                    
                      div {
                        margin-bottom: 1.5rem;
                      }
                    
                      /* 木质容器效果 */
                      input[type="text"],
                      select,
                      #modules > div {
                        background-color: rgba(232, 212, 181, 0.9);
                        background-image: var(--wood-texture);
                        border: 1px solid rgba(139, 90, 43, 0.3);
                        box-shadow: inset 0 0 10px rgba(139, 90, 43, 0.1), 0 2px 5px rgba(0, 0, 0, 0.1);
                        border-radius: 4px;
                        padding: 0.7rem;
                        color: var(--wood-accent);
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        transition: all 0.3s ease;
                      }
                    
                      input[type="text"]:focus,
                      select:focus {
                        outline: none;
                        border-color: rgba(139, 90, 43, 0.6);
                        box-shadow: inset 0 0 10px rgba(139, 90, 43, 0.2), 0 0 0 2px rgba(139, 90, 43, 0.1);
                      }
                    
                      /* 木质按钮效果 */
                      button {
                        padding: 0.7rem 1.2rem;
                        background: linear-gradient(to bottom, var(--wood-medium) 0%, var(--wood-dark) 100%);
                        color: white;
                        border: 1px solid var(--wood-dark);
                        border-radius: 4px;
                        cursor: pointer;
                        transition: all 0.3s ease;
                        margin-right: 0.7rem;
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        font-weight: 500;
                        position: relative;
                        overflow: hidden;
                        text-shadow: 1px 1px 1px rgba(0, 0, 0, 0.3);
                        box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
                      }
                    
                      button:hover {
                        background: linear-gradient(to bottom, var(--wood-dark) 0%, var(--wood-accent) 100%);
                        transform: translateY(-2px);
                        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
                      }
                    
                      button:active {
                        transform: translateY(0);
                        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
                      }
                    
                      /* 木纹按钮纹理 */
                      button::before {
                        content: "";
                        position: absolute;
                        top: 0;
                        left: -100%;
                        width: 100%;
                        height: 100%;
                        background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
                        animation: woodShimmer 3s infinite;
                      }
                    
                      @keyframes woodShimmer {
                        0% { left: -100%; }
                        20% { left: 100%; }
                        100% { left: 100%; }
                      }
                    
                      h3 {
                        color: var(--wood-accent);
                        margin-top: 2rem;
                        margin-bottom: 0.7rem;
                        font-size: 1.2rem;
                        font-weight: 500;
                        padding-bottom: 0.5rem;
                        border-bottom: 2px solid rgba(139, 90, 43, 0.2);
                        position: relative;
                      }
                    
                      /* 标题木纹装饰 */
                      h3::after {
                        content: "";
                        position: absolute;
                        bottom: -2px;
                        left: 0;
                        width: 30%;
                        height: 2px;
                        background-color: var(--wood-accent);
                      }
                    
                      #modules > div {
                        margin-bottom: 1.5rem;
                        padding: 1.2rem;
                        border-radius: 6px;
                        position: relative;
                        overflow: hidden;
                      }
                    
                      /* 模块木纹效果 */
                      #modules > div::before {
                        content: "";
                        position: absolute;
                        top: 0;
                        left: 0;
                        width: 100%;
                        height: 100%;
                        background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='100' height='100' viewBox='0 0 100 100'%3E%3Cpath d='M0 0c20 0 20 5 40 5s20-5 40-5c20 0 20 5 40 5s20-5 40-5v5c-20 0-20 5-40 5s-20-5-40-5c-20 0-20 5-40 5S0 10 0 10V0z' fill='%238b5a2b' fill-opacity='0.05'/%3E%3C/svg%3E");
                        pointer-events: none;
                      }
                    
                      input[type="checkbox"] {
                        margin-right: 0.7rem;
                        cursor: pointer;
                        appearance: none;
                        width: 18px;
                        height: 18px;
                        border: 1px solid var(--wood-dark);
                        border-radius: 3px;
                        background-color: rgba(232, 212, 181, 0.8);
                        position: relative;
                        transition: all 0.2s ease;
                      }
                    
                      input[type="checkbox"]:checked {
                        background-color: var(--wood-dark);
                        border-color: var(--wood-dark);
                      }
                    
                      input[type="checkbox"]:checked::after {
                        content: "";
                        position: absolute;
                        left: 6px;
                        top: 3px;
                        width: 4px;
                        height: 8px;
                        border: solid white;
                        border-width: 0 2px 2px 0;
                        transform: rotate(45deg);
                      }
                    
                      label {
                        margin-right: 1rem;
                        color: var(--wood-accent);
                        font-weight: 500;
                      }
                    
                      select {
                        background-color: rgba(232, 212, 181, 0.9);
                        background-image: var(--wood-texture);
                        border: 1px solid rgba(139, 90, 43, 0.3);
                      }
                    </style>
                    """;


            String css = switch (WebGUI.mode.currentMode) {
                case "传统" -> "";
                case "现代" -> modern;
                case "拟物" -> skeuo;
                case "春节" -> spring;
                case "端午节" -> dwj;
                case "情人节" -> qrj;
                case "小清新" -> xqx;
                case "动物" -> animal;
                case "神奇宝贝" -> pokemon;
                case "温馨" -> wx;
                case "木纹" -> wood;
                default -> "";
            };


            WebClickGUI.sendResponse(exchange, 200, addCSS(css, html));
        } catch (Exception e) {
        }
    }
}
