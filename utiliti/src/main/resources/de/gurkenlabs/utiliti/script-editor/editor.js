(function () {
  const monacoRoot = new URL('../monaco/min/vs', window.location.href).toString().replace(/\/$/, '');
  self.MonacoEnvironment = {
    getWorkerUrl(_moduleId, _label) {
      return `${monacoRoot}/base/worker/workerMain.js`;
    }
  };

  window.addEventListener('unhandledrejection', function (event) {
    if (event && event.reason && (event.reason === 'Canceled' || (event.reason.message && event.reason.message.includes('Canceled')))) {
      event.preventDefault();
    }
  });

  window.require.config({ paths: { vs: monacoRoot } });
  window.require(['vs/editor/editor.main'], function (monaco) {
    monaco.languages.register({ id: 'java', extensions: ['.java'], aliases: ['Java', 'java'] });
    monaco.languages.setMonarchTokensProvider('java', {
      defaultToken: '',
      keywords: ['as', 'assert', 'break', 'case', 'catch', 'class', 'continue', 'def', 'default', 'do', 'else',
        'enum', 'extends', 'final', 'finally', 'for', 'if', 'implements', 'import', 'in', 'instanceof', 'interface',
        'new', 'package', 'private', 'protected', 'public', 'return', 'static', 'switch', 'throw', 'throws', 'trait',
        'try', 'var', 'while', 'synchronized', 'native', 'abstract', 'strictfp', 'transient', 'volatile'],
      constants: ['true', 'false', 'null', 'this', 'super'],
      typeKeywords: ['boolean', 'byte', 'char', 'double', 'float', 'int', 'long', 'short'],
      tokenizer: {
        root: [
          [/\/\*\*/, 'comment.doc', '@comment'],
          [/\/\*/, 'comment', '@comment'],
          [/\/\/.*$/, 'comment'],
          [/\[.*?\]/, 'tag'],
          [/@[A-Za-z_$][\w$]*/, 'annotation'],
          [/[A-Z][\w$]*/, 'type.identifier'],
          [/[a-z_$][\w$]*(?=\s*\()/, 'function'],
          [/[a-z_$][\w$]*/, { cases: { '@keywords': 'keyword', '@constants': 'constant', '@typeKeywords': 'type', '@default': 'identifier' } }],
          [/[{}()[\]]/, '@brackets'],
          [/[;,.]/, 'delimiter'],
          [/[+\-*/%=!<>?:&|~^]+/, 'operator'],
          [/\d+(?:\.\d+)?(?:[dDfFlL])?/, 'number'],
          [/0x[\da-fA-F]+/, 'number.hex'],
          [/"""/, 'string', '@tripleDoubleString'],
          [/'''/, 'string', '@tripleSingleString'],
          [/"/, 'string', '@doubleString'],
          [/'/, 'string', '@singleString'],
          [/\$\{/, 'delimiter.bracket.embed', '@embedded'],
          [/\$/, 'identifier']
        ],
        comment: [[/\*\//, 'comment', '@pop'], [/./, 'comment']],
        doubleString: [[/\\./, 'string.escape'], [/"/, 'string', '@pop'], [/\$\{/, 'delimiter.bracket.embed', '@embedded'], [/\$/, 'identifier'], [/./, 'string']],
        singleString: [[/\\./, 'string.escape'], [/'/, 'string', '@pop'], [/\$/, 'identifier'], [/./, 'string']],
        tripleDoubleString: [[/"""/, 'string', '@pop'], [/\$\{/, 'delimiter.bracket.embed', '@embedded'], [/\$/, 'identifier'], [/./, 'string']],
        tripleSingleString: [[/'''/, 'string', '@pop'], [/\$/, 'identifier'], [/./, 'string']],
        embedded: [[/\}/, 'delimiter.bracket.embed', '@pop'], { include: 'root' }]
      }
    });

    const models = new Map();
    let applying = false;
    let analysisTimer;
    let modelGeneration = 0;
    let breakpointDecorations = [];
    let executionDecorations = [];
    let executionLine = 0;

    monaco.editor.defineTheme('utiliti-dark', {
      base: 'vs-dark',
      inherit: true,
      rules: [
        { token: '', foreground: 'C8D0F5', background: '121214' },
        { token: 'comment', foreground: '565F89', fontStyle: 'italic' },
        { token: 'keyword', foreground: 'BB9AF7' },
        { token: 'keyword.control', foreground: 'BB9AF7' },
        { token: 'keyword.type', foreground: '2AC3DE' },
        { token: 'string', foreground: '9ECE6A' },
        { token: 'string.escape', foreground: '2AC3DE' },
        { token: 'number', foreground: 'FF9E64' },
        { token: 'regexp', foreground: 'F7768E' },
        { token: 'type', foreground: '2AC3DE' },
        { token: 'type.identifier', foreground: '2AC3DE' },
        { token: 'class', foreground: '2AC3DE' },
        { token: 'interface', foreground: '2AC3DE' },
        { token: 'enum', foreground: '2AC3DE' },
        { token: 'annotation', foreground: 'E0AF68' },
        { token: 'function', foreground: '7AA2F7' },
        { token: 'variable', foreground: 'C8D0F5' },
        { token: 'variable.predefined', foreground: 'F7768E' },
        { token: 'parameter', foreground: 'E0AF68' },
        { token: 'property', foreground: '7AA2F7' },
        { token: 'constant', foreground: 'FF9E64' },
        { token: 'operator', foreground: '89DDFF' },
        { token: 'delimiter', foreground: '89DDFF' },
        { token: 'delimiter.bracket', foreground: '89DDFF' },
        { token: 'tag', foreground: 'F7768E' },
        { token: 'metatag', foreground: 'E0AF68' },
        { token: 'metatag.content', foreground: '9ECE6A' },
      ],
      colors: {
        'editor.background': '#121214',
        'editor.foreground': '#C8D0F5',
        'editorLineNumber.foreground': '#3B4261',
        'editorLineNumber.activeForeground': '#C8D0F5',
        'editorCursor.foreground': '#3574F2',
        'editor.selectionBackground': '#3B426180',
        'editor.inactiveSelectionBackground': '#2A2D3A',
        'editor.selectionHighlightBackground': '#2A2D3A80',
        'editor.wordHighlightBackground': '#2A2D3A80',
        'editor.findMatchBackground': '#3574F240',
        'editor.findMatchHighlightBackground': '#2A2D3A',
        'editorHoverWidget.background': '#1E1E23',
        'editorHoverWidget.border': '#373740',
        'editorSuggestWidget.background': '#1E1E23',
        'editorSuggestWidget.border': '#373740',
        'editorSuggestWidget.selectedBackground': '#2A2D3A',
        'editorSuggestWidget.highlightForeground': '#3574F2',
        'editorIndentGuide.background': '#1E1E23',
        'editorIndentGuide.activeBackground': '#373740',
        'editorBracketMatch.background': '#3574F230',
        'editorBracketMatch.border': '#3574F2',
        'editorGutter.background': '#121214',
        'editorWidget.background': '#1E1E23',
        'editorWidget.border': '#373740',
        'input.background': '#24252A',
        'input.foreground': '#C8D0F5',
        'input.border': '#373740',
        'inputOption.activeBorder': '#3574F2',
        'focusBorder': '#3574F2',
        'scrollbar.shadow': '#00000000',
        'scrollbarSlider.background': '#41414B80',
        'scrollbarSlider.hoverBackground': '#41414B',
        'scrollbarSlider.activeBackground': '#555565',
        'minimap.background': '#121214',
        'panel.background': '#121214',
        'panel.border': '#373740',
        'badge.background': '#3574F2',
        'badge.foreground': '#FFFFFF',
        'editorOverviewRuler.border': '#00000000',
        'editorError.foreground': '#F7768E',
        'editorWarning.foreground': '#E0AF68',
        'editorInfo.foreground': '#7AA2F7',
      }
    });


    monaco.editor.defineTheme('utiliti-light', {
      base: 'vs',
      inherit: true,
      rules: [
        { token: '', foreground: '1E1E23', background: 'FFFFFF' },
        { token: 'comment', foreground: '565F89', fontStyle: 'italic' },
        { token: 'keyword', foreground: '7C3AED' },
        { token: 'keyword.control', foreground: '7C3AED' },
        { token: 'keyword.type', foreground: '0284C7' },
        { token: 'string', foreground: '15803D' },
        { token: 'string.escape', foreground: '0284C7' },
        { token: 'number', foreground: 'D97706' },
        { token: 'annotation', foreground: 'D97706' },
        { token: 'function', foreground: '2563EB' },
        { token: 'variable', foreground: '1E1E23' },
        { token: 'property', foreground: '2563EB' },
      ],
      colors: {
        'editor.background': '#FFFFFF',
        'editor.foreground': '#1E1E23',
        'editorLineNumber.foreground': '#94A3B8',
        'editorLineNumber.activeForeground': '#1E1E23',
        'editorCursor.foreground': '#3574F2',
        'editorGutter.background': '#FFFFFF',
        'minimap.background': '#FFFFFF',
      }
    });

    const editor = monaco.editor.create(document.getElementById('editor'), {
      automaticLayout: true,
      theme: 'utiliti-dark',
      fontFamily: "'JetBrains Mono', 'Cascadia Code', Consolas, monospace",
      fontSize: 14,
      lineHeight: 21,
      minimap: { enabled: true, showSlider: 'mouseover' },
      folding: true,
      glyphMargin: true,
      bracketPairColorization: { enabled: true },
      guides: { bracketPairs: true, indentation: true, highlightActiveBracketPair: true },
      stickyScroll: { enabled: true },
      inlayHints: { enabled: 'on' },
      suggest: {
        showIcons: true, showStatusBar: true, preview: true,
        showMethods: true, showFunctions: true, showConstructors: true,
        showFields: true, showVariables: true, showClasses: true,
        showInterfaces: true, showModules: true, showProperties: true,
        showUnits: true, showValues: true, showEnums: true,
        showKeywords: true, showSnippets: true,
        insertMode: 'replace', filterGraceful: true,
        localityBonus: 1.0, shareSuggestSelections: true,
      },
      quickSuggestions: { other: true, comments: false, strings: false },
      parameterHints: { enabled: true },
      semanticHighlighting: { enabled: true },
      smoothScrolling: true,
      cursorSmoothCaretAnimation: 'on',
      padding: { top: 10, bottom: 10 },
      scrollBeyondLastLine: false,
      autoClosingBrackets: 'always',
      autoClosingQuotes: 'always',
      autoSurround: 'languageDefined',
      formatOnPaste: true,
      formatOnType: false,
      linkedEditing: true,
      wordWrap: 'off',
      renderLineHighlight: 'all',
      occurrencesHighlight: 'singleFile',
      selectionHighlight: true,
      foldingStrategy: 'indentation',
      showFoldingControls: 'mouseover',
      renderWhitespace: 'selection'
    });
    window.editor = editor;

    function breakpointLines() {
      const model = editor.getModel();
      if (!model) return [];
      return breakpointDecorations
        .map(id => model.getDecorationRange(id))
        .filter(Boolean)
        .map(range => range.startLineNumber)
        .filter((line, index, lines) => lines.indexOf(line) === index)
        .sort((left, right) => left - right);
    }

    function applyDebugState(state) {
      const model = editor.getModel();
      if (!model) return;
      const lines = (state.breakpoints || []).filter(line => Number.isInteger(line) && line > 0 && line <= model.getLineCount());
      breakpointDecorations = editor.deltaDecorations(breakpointDecorations, lines.map(line => ({
        range: new monaco.Range(line, 1, line, 1),
        options: {
          glyphMarginClassName: 'utiliti-breakpoint',
          glyphMarginHoverMessage: { value: `Breakpoint at line ${line}` },
          stickiness: monaco.editor.TrackedRangeStickiness.NeverGrowsWhenTypingAtEdges
        }
      })));
      if (typeof state.executionLine === 'number') executionLine = state.executionLine;
      executionDecorations = editor.deltaDecorations(executionDecorations, executionLine > 0 && executionLine <= model.getLineCount() ? [{
        range: new monaco.Range(executionLine, 1, executionLine, 1),
        options: { isWholeLine: true, className: 'utiliti-execution-line', glyphMarginClassName: 'utiliti-execution-glyph' }
      }] : []);
    }

    editor.onMouseDown(event => {
      if (event.target.type !== monaco.editor.MouseTargetType.GUTTER_GLYPH_MARGIN || !event.target.position) return;
      const line = event.target.position.lineNumber;
      const lines = breakpointLines();
      const next = lines.includes(line) ? lines.filter(candidate => candidate !== line) : [...lines, line];
      applyDebugState({ breakpoints: next });
      query('breakpoints', { lines: breakpointLines() }).catch(console.error);
      event.event.preventDefault();
    });

    function query(method, payload) {
      return new Promise((resolve, reject) => {
        if (!window.cefQuery) {
          reject(new Error('The utiLITI editor bridge is unavailable.'));
          return;
        }
        window.cefQuery({
          request: JSON.stringify({ method, payload: payload || {} }),
          onSuccess(response) {
            try {
              const result = JSON.parse(response);
              result.ok ? resolve(result.value) : reject(new Error(result.error));
            } catch (e) {
              reject(e);
            }
          },
          onFailure(code, message) { reject(new Error(`${code}: ${message}`)); }
        });
      });
    }

    function currentPosition(position) {
      return { line: position.lineNumber, column: position.column };
    }

    function completionKind(kind) {
      return {
        CLASS: monaco.languages.CompletionItemKind.Class,
        CONSTRUCTOR: monaco.languages.CompletionItemKind.Constructor,
        METHOD: monaco.languages.CompletionItemKind.Method,
        FIELD: monaco.languages.CompletionItemKind.Field,
        PROPERTY: monaco.languages.CompletionItemKind.Property,
        VARIABLE: monaco.languages.CompletionItemKind.Variable,
        KEYWORD: monaco.languages.CompletionItemKind.Keyword,
        SNIPPET: monaco.languages.CompletionItemKind.Snippet
      }[kind] ?? monaco.languages.CompletionItemKind.Text;
    }

    ['java'].forEach(lang => {
      monaco.languages.registerCompletionItemProvider(lang, {
        triggerCharacters: ['.', '(', ',', '@'],
        async provideCompletionItems(model, position) {
          try {
            const gen = modelGeneration;
            const value = await query('complete', currentPosition(position));
            if (gen !== modelGeneration || editor.getModel() !== model) return { suggestions: [] };
            const word = model.getWordUntilPosition(position);
            const lineContent = model.getLineContent(position.lineNumber);
            const lineUntilPos = lineContent.substring(0, position.column - 1);
            const atIndex = lineUntilPos.lastIndexOf('@');
            const isAnnotationContext = atIndex >= 0 && /^@\s*[A-Za-z0-9_$]*$/.test(lineUntilPos.substring(atIndex));

            const wordRange = new monaco.Range(position.lineNumber, word.startColumn, position.lineNumber, Math.max(word.endColumn, position.column));
            const atRange = isAnnotationContext ? new monaco.Range(position.lineNumber, atIndex + 1, position.lineNumber, Math.max(word.endColumn, position.column)) : wordRange;

            return {
              suggestions: (value.items || []).map(item => {
                const rawLabel = item.label || '';
                const rawInsert = item.insertText || rawLabel;
                const isSnippet = item.kind === 'SNIPPET' || (typeof rawInsert === 'string' && rawInsert.includes('${'));

                let label = rawLabel;
                let insertText = rawInsert;
                let filterText = '';
                let targetRange = wordRange;

                const isOverrideMethod = label.startsWith('@Override ') || label.includes(' [Override]');
                const isOverride = label === '@Override' || label === 'Override' || isOverrideMethod;
                const isProp = label.includes('ScriptProperty') || label === 'prop' || label === 'scriptproperty';
                const isInfo = label.includes('ScriptInfo');

                if (isAnnotationContext) {
                  targetRange = atRange;
                  if (!insertText.startsWith('@') && !isSnippet) {
                    insertText = '@' + insertText;
                  }
                  if (!label.startsWith('@') && !isSnippet) {
                    label = '@' + label;
                  }
                  const baseFilter = (label || insertText).replace(/\(.*\)/, '').replace(/\[.*\]/, '').split('\n')[0].trim();
                  filterText = baseFilter.startsWith('@') ? baseFilter : ('@' + baseFilter);
                } else {
                  targetRange = wordRange;
                  const baseFilter = (label || insertText).replace(/^@/, '').replace(/\(.*\)/, '').replace(/\[.*\]/, '').split('\n')[0].trim();
                  filterText = baseFilter;
                }

                const sortPrefix = isOverrideMethod ? '000_'
                  : isOverride ? '001_'
                  : isProp ? '002_'
                  : isInfo ? '003_'
                  : isSnippet ? '010_'
                  : item.kind === 'PROPERTY' ? '020_'
                  : item.kind === 'FIELD' || item.kind === 'METHOD' ? '030_'
                  : '100_';
                const detailStr = item.detail ? (item.detail + (item.returnType ? `  ${item.returnType}` : '')) : (item.returnType || '');
                const suggestion = {
                  label: label,
                  kind: completionKind(item.kind),
                  detail: detailStr,
                  insertText: insertText,
                  insertTextRules: isSnippet ? monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet : undefined,
                  filterText: filterText,
                  sortText: sortPrefix + label,
                  range: targetRange,
                  additionalTextEdits: (item.additionalTextEdits || []).map(edit => ({
                    range: new monaco.Range(edit.startLine + 1, edit.startColumn + 1, edit.endLine + 1, edit.endColumn + 1),
                    text: edit.text
                  }))
                };
                if (item.documentation) {
                  suggestion.documentation = { value: item.documentation };
                }
                return suggestion;
              }),
              incomplete: false
            };
          } catch (error) {
            console.error(error);
            return { suggestions: [], incomplete: false };
          }

        }
      });

      monaco.languages.registerHoverProvider(lang, {
        async provideHover(_model, position) {
          try {
            const gen = modelGeneration;
            const value = await query('hover', currentPosition(position));
            if (gen !== modelGeneration || editor.getModel() !== _model) return null;
            return value.markdown ? { contents: [{ value: value.markdown }] } : null;
          } catch (error) {
            console.error(error);
            return null;
          }
        }
      });

      monaco.languages.registerSignatureHelpProvider(lang, {
        signatureHelpTriggerCharacters: ['(', ','],
        async provideSignatureHelp(_model, position) {
          try {
            const gen = modelGeneration;
            const value = await query('signature', currentPosition(position));
            if (gen !== modelGeneration || editor.getModel() !== _model) return null;
            return {
              value: {
                signatures: value.signatures.map(item => ({
                  label: item.label,
                  documentation: item.documentation,
                  parameters: (item.parameters || []).map(param => ({
                    label: param.label,
                    documentation: param.documentation
                  }))
                })),
                activeSignature: value.activeSignature,
                activeParameter: value.activeParameter
              },
              dispose() {}
            };
          } catch (error) {
            console.error(error);
            return null;
          }
        }
      });

      monaco.languages.registerDefinitionProvider(lang, {
        triggerCharacters: [],
        async provideDefinition(model, position) {
          try {
            const gen = modelGeneration;
            const value = await query('definition', currentPosition(position));
            if (gen !== modelGeneration || editor.getModel() !== model) return null;
            if (!value.uri) return null;
            return {
              uri: monaco.Uri.parse(value.uri),
              range: new monaco.Range(value.line + 1, value.column + 1, value.line + 1, value.column + 1)
            };
          } catch (error) {
            console.error(error);
            return null;
          }
        }
      });

      monaco.languages.registerCodeActionProvider(lang, {
        async provideCodeActions(model, range) {
          try {
            const gen = modelGeneration;
            const value = await query('codeActions', {
              startLine: range.startLineNumber, startColumn: range.startColumn,
              endLine: range.endLineNumber, endColumn: range.endColumn
            });
            if (gen !== modelGeneration || editor.getModel() !== model) return { actions: [], dispose() {} };
            return {
              actions: value.actions.map(action => ({
                title: action.title,
                kind: action.kind || 'quickfix',
                edit: {
                  edits: action.edits.map(edit => ({
                    resource: model.uri,
                    textEdit: {
                      range: new monaco.Range(edit.startLine + 1, edit.startColumn + 1, edit.endLine + 1, edit.endColumn + 1),
                      text: edit.text.replace(/\\n/g, '\n')
                    },
                    versionId: model.getVersionId()
                  }))
                }
              })),
              dispose() {}
            };
          } catch (error) {
            console.error(error);
            return { actions: [], dispose() {} };
          }
        },
        providedCodeActionKinds: ['quickfix', 'source']
      });

      monaco.languages.registerDocumentFormattingEditProvider(lang, {
        async provideDocumentFormattingEdits(model) {
          try {
            const gen = modelGeneration;
            const value = await query('format');
            if (gen !== modelGeneration || editor.getModel() !== model || !value || typeof value.text !== 'string') return [];
            const fullRange = model.getFullModelRange();
            return [{
              range: fullRange,
              text: value.text
            }];
          } catch (error) {
            console.error('Format document error:', error);
            return [];
          }
        }
      });

      monaco.languages.registerRenameProvider(lang, {
        async provideRenameEdits(model, position, newName) {
          try {
            const gen = modelGeneration;
            const value = await query('rename', {
              line: position.lineNumber,
              column: position.column,
              newName: newName
            });
            if (gen !== modelGeneration || editor.getModel() !== model || !value || !value.edits) return { edits: [] };
            return {
              edits: value.edits.map(edit => ({
                resource: model.uri,
                textEdit: {
                  range: new monaco.Range(
                    edit.startLine + 1, edit.startColumn + 1,
                    edit.endLine + 1, edit.endColumn + 1
                  ),
                  text: edit.newText
                }
              }))
            };
          } catch (error) {
            console.error('Rename error:', error);
            return { edits: [] };
          }
        }
      });

      monaco.languages.registerDocumentSymbolProvider(lang, {
        async provideDocumentSymbols(model) {
          try {
            const gen = modelGeneration;
            const value = await query('symbols');
            if (gen !== modelGeneration || editor.getModel() !== model) return [];
            function toSymbol(sym) {
              const kind = {
                CLASS: monaco.languages.SymbolKind.Class,
                FIELD: monaco.languages.SymbolKind.Field,
                METHOD: monaco.languages.SymbolKind.Method,
                CONSTRUCTOR: monaco.languages.SymbolKind.Constructor,
                PROPERTY: monaco.languages.SymbolKind.Property,
                VARIABLE: monaco.languages.SymbolKind.Variable
              }[sym.kind] ?? monaco.languages.SymbolKind.Variable;
              const range = new monaco.Range(sym.startLine + 1, sym.startColumn + 1, sym.endLine + 1, sym.endColumn + 1);
              return {
                name: sym.name,
                detail: sym.detail || '',
                kind,
                range,
                selectionRange: range,
                children: (sym.children || []).map(toSymbol)
              };
            }
            return value.symbols.map(toSymbol);
          } catch (error) {
            console.error(error);
            return [];
          }
        }
      });
    });

    let lastBreakpointLinesStr = '';
    function checkBreakpointsChanged() {
      const lines = breakpointLines();
      const currentStr = lines.join(',');
      if (currentStr !== lastBreakpointLinesStr) {
        lastBreakpointLinesStr = currentStr;
        query('breakpoints', { lines }).catch(console.error);
      }
    }

    editor.onDidChangeModelContent(event => {
      if (applying) return;
      const changes = event.changes.map(change => ({
        offset: change.rangeOffset,
        length: change.rangeLength,
        text: change.text
      }));
      query('change', { changes }).catch(console.error);
      clearTimeout(analysisTimer);
      analysisTimer = setTimeout(analyze, 220);
      setTimeout(checkBreakpointsChanged, 0);
    });

    let cursorTimer;
    editor.onDidChangeCursorPosition(event => {
      clearTimeout(cursorTimer);
      cursorTimer = setTimeout(() => {
        query('cursor', currentPosition(event.position)).catch(console.error);
      }, 50);
    });

    editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.Tab,
      () => query('switchWorkspaceMode', { mode: 'cycle' }).catch(console.error));
    editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyMod.Shift | monaco.KeyCode.KeyM,
      () => query('switchWorkspaceMode', { mode: 'map' }).catch(console.error));
    editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyMod.Shift | monaco.KeyCode.KeyS,
      () => query('switchWorkspaceMode', { mode: 'script' }).catch(console.error));
    editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.KeyS, () => query('save').catch(console.error));
    editor.addCommand(monaco.KeyMod.Alt | monaco.KeyCode.Enter, () => {
      editor.getAction('editor.action.quickFix')?.run();
    });
    editor.addCommand(monaco.KeyCode.F12, () => {
      editor.getAction('editor.action.revealDefinition')?.run();
    });
    editor.addCommand(monaco.KeyCode.F5, () => query('debugCommand', { command: 'continue' }).catch(console.error));
    editor.addCommand(monaco.KeyCode.F10, () => query('debugCommand', { command: 'stepOver' }).catch(console.error));
    editor.addCommand(monaco.KeyCode.F11, () => query('debugCommand', { command: 'stepInto' }).catch(console.error));
    editor.addCommand(monaco.KeyMod.Shift | monaco.KeyCode.F11,
      () => query('debugCommand', { command: 'stepOut' }).catch(console.error));
    editor.addCommand(monaco.KeyMod.Shift | monaco.KeyCode.F10,
      () => query('debugCommand', { command: 'run' }).catch(console.error));
    editor.addCommand(monaco.KeyMod.Shift | monaco.KeyCode.F9,
      () => query('debugCommand', { command: 'debug' }).catch(console.error));
    editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.F2,
      () => query('debugCommand', { command: 'stop' }).catch(console.error));

    async function analyze() {
      const model = editor.getModel();
      if (!model) return;
      const gen = modelGeneration;
      try {
        const value = await query('analyze');
        if (gen !== modelGeneration || editor.getModel() !== model) return;
        monaco.editor.setModelMarkers(model, 'litiengine', value.diagnostics.map(item => ({
          severity: item.severity === 'ERROR' ? monaco.MarkerSeverity.Error
            : item.severity === 'WARNING' ? monaco.MarkerSeverity.Warning : monaco.MarkerSeverity.Info,
          message: item.message,
          startLineNumber: item.line,
          startColumn: item.column,
          endLineNumber: item.line,
          endColumn: item.column + 1
        })));
      } catch (error) {
        console.error(error);
      }
    }

    window.addEventListener('resize', function () {
      if (typeof editor !== 'undefined' && typeof editor.layout === 'function') {
        editor.layout();
      }
    });

    window.utilitiEditor = {
      receive(encoded) {
        try {
          let jsonString;
          try {
            const raw = atob(encoded);
            const bytes = Uint8Array.from(raw, c => c.charCodeAt(0));
            jsonString = new TextDecoder().decode(bytes);
          } catch (e) {
            jsonString = encoded;
          }
          const { method, payload } = JSON.parse(jsonString);
          if (method === 'open') {
            const uri = monaco.Uri.parse(payload.uri);
            let model = monaco.editor.getModel(uri) || models.get(payload.uri);
            if (model && model.isDisposed()) {
              models.delete(payload.uri);
              models.delete(uri.toString());
              model = null;
            }
            if (!model) {
              const canonical = uri.toString().toLowerCase();
              for (const m of monaco.editor.getModels()) {
                if (m.uri.toString().toLowerCase() === canonical) {
                  if (m.isDisposed()) {
                    models.delete(m.uri.toString());
                  } else {
                    model = m;
                  }
                  break;
                }
              }
            }
            modelGeneration++;
            clearTimeout(analysisTimer);
            applying = true;
            try {
              if (!model || model.isDisposed()) {
                const existing = monaco.editor.getModel(uri);
                if (existing) existing.dispose();
                model = monaco.editor.createModel(payload.text, payload.language || 'java', uri);
              } else {
                if (model.getValue() !== payload.text) {
                  model.setValue(payload.text);
                }
              }
              models.set(payload.uri, model);
              models.set(uri.toString(), model);
              editor.setModel(model);
              applyDebugState(payload);
              setTimeout(() => {
                if (typeof editor !== 'undefined' && typeof editor.layout === 'function') {
                  editor.layout();
                }
              }, 10);
            } catch (e) {
              console.error('Monaco model set error:', e);
              if (model && !model.isDisposed()) {
                try { editor.setModel(model); } catch (ignored) {}
              }
            } finally {
              applying = false;
            }
            analyze();
          } else if (method === 'closeModel') {
            const uri = monaco.Uri.parse(payload.uri);
            let model = monaco.editor.getModel(uri) || models.get(payload.uri);
            if (model) {
              try { model.dispose(); } catch (ignored) {}
              models.delete(payload.uri);
              models.delete(uri.toString());
            }
          } else if (method === 'theme') {
            const isDark = Boolean(payload.dark);
            monaco.editor.setTheme(isDark ? 'utiliti-dark' : 'utiliti-light');
            const bg = isDark ? '#121214' : '#FFFFFF';
            const fg = isDark ? '#C8D0F5' : '#1E1E23';
            document.documentElement.style.backgroundColor = bg;
            document.documentElement.style.color = fg;
            document.body.style.backgroundColor = bg;
            document.body.style.color = fg;
            const el = document.getElementById('editor');
            if (el) {
              el.style.backgroundColor = bg;
              el.style.color = fg;
            }
          } else if (method === 'focus') {

            try { editor.focus(); } catch (ignored) {}
          } else if (method === 'blur') {
            try {
              const active = document.activeElement;
              if (active && typeof active.blur === 'function') active.blur();
            } catch (ignored) {}
          } else if (method === 'revealLine') {
            if (payload.line) {
              try {
                editor.revealLineInCenter(payload.line);
                editor.setPosition({ lineNumber: payload.line, column: payload.column || 1 });
                editor.focus();
              } catch (ignored) {}
            }
          } else if (method === 'insertText') {
            if (payload.text && typeof editor !== 'undefined') {
              try {
                const selection = editor.getSelection();
                const range = selection || new monaco.Range(1, 1, 1, 1);
                const id = { major: 1, minor: 1 };
                const op = { identifier: id, range: range, text: payload.text, forceMoveMarkers: true };
                editor.executeEdits('insertText', [op]);
                editor.focus();
              } catch (ignored) {}
            }
          } else if (method === 'triggerFormat') {
            if (typeof editor !== 'undefined') {
              try { editor.getAction('editor.action.formatDocument')?.run(); } catch (ignored) {}
            }
          } else if (method === 'debugState') {
            applyDebugState(payload);
          }
        } catch (globalError) {
          console.error('Error in window.utilitiEditor.receive:', globalError);
        }
      }
    };

    query('ready')
      .then(() => { window.utilitiEditorReady = true; })
      .catch(error => window.utilitiReportStartupError(error));
  }, function (error) {
    window.utilitiReportStartupError(error);
  });
})();
