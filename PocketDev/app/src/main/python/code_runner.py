"""
PocketDev - Python Code Runner
This module is used by Chaquopy to execute user Python code
and capture stdout/stderr output safely.
Supports real-time interactive input() via callback.
"""
import sys
import io
import traceback


class InteractiveInput:
    """Custom stdin that calls a Java/Kotlin callback for each input() call."""
    
    def __init__(self, input_callback=None):
        self.input_callback = input_callback
        self.buffer = ""
    
    def readline(self):
        if self.input_callback is not None:
            # Call back to Kotlin to get real-time input
            result = self.input_callback()
            if result is None:
                return ""
            return str(result) + "\n"
        return "\n"
    
    def read(self, size=-1):
        return self.readline()
    
    def readable(self):
        return True
    
    def writable(self):
        return False
    
    def seekable(self):
        return False
    
    def close(self):
        pass
    
    def __iter__(self):
        return self
    
    def __next__(self):
        line = self.readline()
        if not line:
            raise StopIteration
        return line


class StreamingOutput:
    """Custom stdout that calls a callback for each write, enabling real-time output."""
    
    def __init__(self, output_callback=None):
        self.output_callback = output_callback
        self.buffer = io.StringIO()
    
    def write(self, text):
        self.buffer.write(text)
        if self.output_callback is not None and text:
            self.output_callback(text)
    
    def flush(self):
        pass
    
    def getvalue(self):
        return self.buffer.getvalue()
    
    def writable(self):
        return True
    
    def readable(self):
        return False
    
    def seekable(self):
        return False
    
    def close(self):
        pass


def execute_code(code, std_input=""):
    """
    Execute Python code safely and return output/error as a dict.
    Uses pre-provided std_input (legacy mode, no interactivity).
    """
    stdout_capture = io.StringIO()
    stderr_capture = io.StringIO()
    stdin_mock = io.StringIO(std_input)

    old_stdout = sys.stdout
    old_stderr = sys.stderr
    old_stdin = sys.stdin

    output = ""
    error = None

    try:
        sys.stdout = stdout_capture
        sys.stderr = stderr_capture
        sys.stdin = stdin_mock

        exec_globals = {
            '__name__': '__main__',
            '__builtins__': __builtins__,
        }

        exec(compile(code, '<user_code>', 'exec'), exec_globals)

        output = stdout_capture.getvalue()
        err_output = stderr_capture.getvalue()
        if err_output.strip():
            error = err_output

    except SyntaxError as e:
        error = format_syntax_error(e)
    except SystemExit as e:
        output = stdout_capture.getvalue()
        if e.code is not None and e.code != 0:
            error = f"SystemExit: {e.code}"
    except Exception as e:
        output = stdout_capture.getvalue()
        tb = traceback.format_exc()
        error = format_traceback(tb, e)
    finally:
        sys.stdout = old_stdout
        sys.stderr = old_stderr
        sys.stdin = old_stdin

    return {
        'output': output,
        'error': error or ''
    }


def execute_code_interactive(code, input_callback=None, output_callback=None):
    """
    Execute Python code with real-time interactive input/output.
    
    Args:
        code (str): Python code to execute
        input_callback: A callable that blocks until user provides input (called from Kotlin)
        output_callback: A callable that receives real-time output text
    
    Returns:
        dict: {'output': str, 'error': str or None}
    """
    interactive_stdin = InteractiveInput(input_callback)
    streaming_stdout = StreamingOutput(output_callback)
    stderr_capture = io.StringIO()

    old_stdout = sys.stdout
    old_stderr = sys.stderr
    old_stdin = sys.stdin

    error = None

    try:
        sys.stdout = streaming_stdout
        sys.stderr = stderr_capture
        sys.stdin = interactive_stdin

        exec_globals = {
            '__name__': '__main__',
            '__builtins__': __builtins__,
        }

        exec(compile(code, '<user_code>', 'exec'), exec_globals)

        err_output = stderr_capture.getvalue()
        if err_output.strip():
            error = err_output

    except SyntaxError as e:
        error = format_syntax_error(e)
    except SystemExit as e:
        if e.code is not None and e.code != 0:
            error = f"SystemExit: {e.code}"
    except Exception as e:
        tb = traceback.format_exc()
        error = format_traceback(tb, e)
    finally:
        sys.stdout = old_stdout
        sys.stderr = old_stderr
        sys.stdin = old_stdin

    return {
        'output': streaming_stdout.getvalue(),
        'error': error or ''
    }


def format_syntax_error(e):
    """Format a syntax error in a beginner-friendly way."""
    msg = f"SyntaxError: {e.msg}"
    if e.lineno:
        msg += f"\n  Line {e.lineno}"
    if e.text:
        msg += f": {e.text.strip()}"
    if e.offset:
        pointer = ' ' * (e.offset - 1) + '^'
        if e.text:
            msg += f"\n  {e.text.rstrip()}"
            msg += f"\n  {pointer}"
    return msg


def format_traceback(tb, exception):
    """Format traceback to only show user-relevant parts."""
    lines = tb.strip().split('\n')
    user_lines = []
    in_user_code = False

    for i, line in enumerate(lines):
        if '<user_code>' in line:
            in_user_code = True
        if in_user_code:
            user_lines.append(line)

    if user_lines:
        return '\n'.join(user_lines)

    return f"{type(exception).__name__}: {str(exception)}"


def check_syntax(code):
    """Check code syntax without executing it."""
    try:
        compile(code, '<user_code>', 'exec')
        return {'valid': True, 'error': None}
    except SyntaxError as e:
        return {'valid': False, 'error': format_syntax_error(e)}
