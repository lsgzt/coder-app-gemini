"""
PocketDev - Python Code Runner
This module is used by Chaquopy to execute user Python code
and capture stdout/stderr output safely.
"""
import sys
import io
import traceback
import signal
import threading


class TimeoutException(Exception):
    pass


def execute_code(code, std_input=""):
    """
    Execute Python code safely and return output/error as a dict.

    Args:
        code (str): Python code to execute
        std_input (str): Standard input to provide to the script

    Returns:
        dict: {'output': str, 'error': str or None}
    """
    # Capture stdout and stderr
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

        # Create a clean namespace for execution
        exec_globals = {
            '__name__': '__main__',
            '__builtins__': __builtins__,
        }

        # Execute the code
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
        # Format the traceback nicely for students
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


def format_syntax_error(e):
    """Format a syntax error in a beginner-friendly way."""
    msg = f"SyntaxError: {e.msg}"
    if e.lineno:
        msg += f"\n  Line {e.lineno}"
    if e.text:
        msg += f": {e.text.strip()}"
    if e.offset:
        # Show pointer to the error location
        pointer = ' ' * (e.offset - 1) + '^'
        if e.text:
            msg += f"\n  {e.text.rstrip()}"
            msg += f"\n  {pointer}"
    return msg


def format_traceback(tb, exception):
    """Format traceback to only show user-relevant parts."""
    lines = tb.strip().split('\n')
    # Find the last "user_code" frame
    user_lines = []
    in_user_code = False

    for i, line in enumerate(lines):
        if '<user_code>' in line:
            in_user_code = True
        if in_user_code:
            user_lines.append(line)

    if user_lines:
        return '\n'.join(user_lines)

    # If we can't find user code frames, return cleaned traceback
    return f"{type(exception).__name__}: {str(exception)}"


def check_syntax(code):
    """
    Check code syntax without executing it.

    Returns:
        dict: {'valid': bool, 'error': str or None}
    """
    try:
        compile(code, '<user_code>', 'exec')
        return {'valid': True, 'error': None}
    except SyntaxError as e:
        return {'valid': False, 'error': format_syntax_error(e)}
