import uvicorn
import datetime
import random
from mcp.server.fastmcp import FastMCP

mcp = FastMCP("LearningMCP")

@mcp.tool()
async def echo_test(message: str) -> str:
    """Echo back the message received from the client."""
    return f"MCP Server 收到你的消息了: {message}"

@mcp.tool()
async def get_current_time() -> str:
    """Get the current date and time."""
    return f"当前时间: {datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')}"

@mcp.tool()
async def get_random_number(min_val: int = 1, max_val: int = 100) -> str:
    """Generate a random number within the specified range.
    
    Args:
        min_val: Minimum value (default: 1)
        max_val: Maximum value (default: 100)
    """
    num = random.randint(min_val, max_val)
    return f"随机数: {num} (范围: {min_val}-{max_val})"

@mcp.tool()
async def calculate(a: int, b: int, operation: str = "add") -> str:
    """Perform basic arithmetic operations on two numbers.
    
    Args:
        a: First number
        b: Second number
        operation: Operation to perform (add, subtract, multiply, divide)
    """
    if operation == "add":
        result = a + b
        op_name = "加法"
    elif operation == "subtract":
        result = a - b
        op_name = "减法"
    elif operation == "multiply":
        result = a * b
        op_name = "乘法"
    elif operation == "divide":
        if b == 0:
            return "错误: 除数不能为零"
        result = a / b
        op_name = "除法"
    else:
        return f"不支持的操作: {operation}"
    return f"{op_name}结果: {a} {operation} {b} = {result}"

if __name__ == "__main__":
    print("=" * 50)
    print("MCP Server 启动中...")
    print("服务地址: http://localhost:8000")
    print("SSE 端点: http://localhost:8000/sse")
    print("可用工具: echo_test, get_current_time, get_random_number, calculate")
    print("=" * 50)
    mcp.run(transport="sse")
