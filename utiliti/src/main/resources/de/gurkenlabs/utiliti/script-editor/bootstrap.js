(function () {
  let reported = false;

  function describe(error, source, line, column) {
    const message = error && error.message ? error.message : String(error || 'Unknown JavaScript error');
    const location = source ? ` (${source}:${line || 0}:${column || 0})` : '';
    const stack = error && error.stack ? `\n${error.stack}` : '';
    return `${message}${location}${stack}`;
  }

  window.utilitiReportStartupError = function (error, source, line, column) {
    const detail = describe(error, source, line, column);
    if (window.utilitiEditorReady) {
      console.error(detail);
      return;
    }
    window.utilitiStartupError = detail;
    if (reported || !window.cefQuery) return;
    reported = true;
    window.cefQuery({
      request: JSON.stringify({ method: 'startupError', payload: { detail } }),
      onFailure() { reported = false; }
    });
  };

  window.addEventListener('error', function (event) {
    window.utilitiReportStartupError(event.error || event.message, event.filename, event.lineno, event.colno);
  });
  window.addEventListener('unhandledrejection', function (event) {
    const reason = event && event.reason;
    if (reason === 'Canceled' || (reason && reason.message && reason.message.includes('Canceled'))) return;
    window.utilitiReportStartupError(reason);
  });
})();
