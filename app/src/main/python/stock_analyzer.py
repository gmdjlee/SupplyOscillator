"""
주식 분석 통합 모듈
deposit_scraper와 stock_data_fetcher를 통합하여 사용
"""

import json
import sys
import traceback

# 명시적으로 모듈 import
try:
    from deposit_scraper import scrape_deposit_data, get_latest_data
    from stock_data_fetcher import (
        search_stock,
        get_stock_data,
        get_stock_name,
        get_all_stocks
    )
except ImportError as e:
    print(f"Import Error: {e}", file=sys.stderr)
    traceback.print_exc()


def search_stock_wrapper(query):
    """
    종목 검색

    Parameters:
    -----------
    query : str
        검색어 (종목명 또는 코드)

    Returns:
    --------
    str
        JSON 문자열: {"ticker": "005930", "name": "삼성전자"} or {"error": "..."}
    """
    try:
        matches = search_stock(query)

        if not matches:
            return json.dumps({"error": "종목을 찾을 수 없습니다"}, ensure_ascii=False)

        # 가장 관련성 높은 종목 반환
        return json.dumps(matches[0], ensure_ascii=False)

    except Exception as e:
        error_msg = f"검색 오류: {str(e)}\n{traceback.format_exc()}"
        print(error_msg, file=sys.stderr)
        return json.dumps({"error": str(e)}, ensure_ascii=False)


def get_stock_analysis(ticker, days=180):
    """
    종목의 시가총액 및 투자자별 거래 데이터 수집

    Parameters:
    -----------
    ticker : str
        종목 코드
    days : int
        분석 기간 (일)

    Returns:
    --------
    str
        JSON 문자열
    """
    try:
        # 종목 데이터 수집
        data = get_stock_data(ticker, days)

        if data is None:
            return json.dumps({
                "error": "데이터를 가져올 수 없습니다"
            }, ensure_ascii=False)

        # 종목명 추가
        stock_name = get_stock_name(ticker)
        data["ticker"] = ticker
        data["name"] = stock_name or ticker

        return json.dumps(data, ensure_ascii=False)

    except Exception as e:
        error_msg = f"분석 오류: {str(e)}\n{traceback.format_exc()}"
        print(error_msg, file=sys.stderr)
        return json.dumps({"error": str(e)}, ensure_ascii=False)


def get_market_deposit_data(num_pages=5):
    """
    증시 자금 동향 데이터 수집 (고객예탁금, 신용잔고)

    Parameters:
    -----------
    num_pages : int
        수집할 페이지 수

    Returns:
    --------
    str
        JSON 문자열
    """
    try:
        print(f"증시 자금 동향 수집 시작: {num_pages}페이지", file=sys.stderr)

        # deposit_scraper 함수 호출
        data = scrape_deposit_data(num_pages)

        print(f"데이터 수집 결과: {type(data)}", file=sys.stderr)

        if data is None or not data:
            error_msg = "시장 데이터를 가져올 수 없습니다 (데이터 없음)"
            print(error_msg, file=sys.stderr)
            return json.dumps({"error": error_msg}, ensure_ascii=False)

        # 데이터 검증
        if not isinstance(data, dict):
            error_msg = f"잘못된 데이터 형식: {type(data)}"
            print(error_msg, file=sys.stderr)
            return json.dumps({"error": error_msg}, ensure_ascii=False)

        required_keys = ['dates', 'deposit_amounts', 'deposit_changes',
                         'credit_amounts', 'credit_changes']
        missing_keys = [key for key in required_keys if key not in data]

        if missing_keys:
            error_msg = f"필수 키 누락: {missing_keys}"
            print(error_msg, file=sys.stderr)
            return json.dumps({"error": error_msg}, ensure_ascii=False)

        # 데이터가 비어있는지 확인
        if not data.get('dates') or len(data['dates']) == 0:
            error_msg = "수집된 데이터가 비어있습니다"
            print(error_msg, file=sys.stderr)
            return json.dumps({"error": error_msg}, ensure_ascii=False)

        print(f"데이터 수집 성공: {len(data['dates'])}개", file=sys.stderr)
        return json.dumps(data, ensure_ascii=False)

    except Exception as e:
        error_msg = f"증시 데이터 수집 오류: {str(e)}\n{traceback.format_exc()}"
        print(error_msg, file=sys.stderr)
        return json.dumps({"error": str(e)}, ensure_ascii=False)


def get_latest_market_data():
    """
    최신 증시 자금 동향 (1페이지)

    Returns:
    --------
    str
        JSON 문자열
    """
    try:
        print("최신 증시 자금 동향 수집 시작", file=sys.stderr)

        data = get_latest_data()

        if data is None or not data:
            return json.dumps({
                "error": "최신 데이터를 가져올 수 없습니다"
            }, ensure_ascii=False)

        print(f"최신 데이터 수집 성공: {len(data.get('dates', []))}개", file=sys.stderr)
        return json.dumps(data, ensure_ascii=False)

    except Exception as e:
        error_msg = f"최신 데이터 오류: {str(e)}\n{traceback.format_exc()}"
        print(error_msg, file=sys.stderr)
        return json.dumps({"error": str(e)}, ensure_ascii=False)


def get_all_stocks_list():
    """
    전체 종목 리스트 가져오기 (자동완성용)

    Returns:
    --------
    str
        JSON 문자열: [{"ticker": "005930", "name": "삼성전자"}, ...]
    """
    try:
        stocks = get_all_stocks()
        return json.dumps(stocks, ensure_ascii=False)

    except Exception as e:
        error_msg = f"종목 리스트 오류: {str(e)}\n{traceback.format_exc()}"
        print(error_msg, file=sys.stderr)
        return json.dumps({"error": str(e)}, ensure_ascii=False)


# 테스트용 메인
if __name__ == "__main__":
    print("=== 주식 분석 통합 모듈 테스트 ===\n")

    # 1. 종목 검색
    print("1. 종목 검색 테스트")
    result = search_stock_wrapper("삼성전자")
    print(result)
    print()

    # 2. 종목 분석
    print("2. 종목 분석 테스트")
    result = get_stock_analysis("005930", days=60)
    data = json.loads(result)
    if "error" not in data:
        print(f"종목: {data['name']}")
        print(f"데이터 개수: {len(data['dates'])}개")
        print(f"날짜 범위: {data['dates'][0]} ~ {data['dates'][-1]}")
    else:
        print(f"오류: {data['error']}")
    print()

    # 3. 증시 자금 동향
    print("3. 증시 자금 동향 테스트")
    result = get_market_deposit_data(num_pages=2)
    data = json.loads(result)
    if "error" not in data:
        print(f"데이터 개수: {len(data['dates'])}개")
        if data['dates']:
            idx = -1
            print(f"최신 날짜: {data['dates'][idx]}")
            print(f"고객예탁금: {data['deposit_amounts'][idx]:,.0f}억원")
            print(f"신용잔고: {data['credit_amounts'][idx]:,.0f}억원")
    else:
        print(f"오류: {data['error']}")