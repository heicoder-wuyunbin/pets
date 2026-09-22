package com.pets.report.adapter.in;

import com.pets.common.web.R;
import com.pets.report.application.RankService;
import com.pets.report.application.ReportService;
import com.pets.report.application.StatService;
import com.pets.report.application.dto.DailyReportVO;
import com.pets.report.application.dto.LostMemberVO;
import com.pets.report.application.dto.MemberStatVO;
import com.pets.report.application.dto.RankVO;
import com.pets.report.application.dto.TrendVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final StatService statService;
    private final RankService rankService;

    /** 营业日报区间查询 */
    @PostMapping("/daily")
    public R<List<DailyReportVO>> daily(@RequestBody Map<String, String> body) {
        LocalDate start = body.get("startDate") != null ? LocalDate.parse(body.get("startDate")) : null;
        LocalDate end = body.get("endDate") != null ? LocalDate.parse(body.get("endDate")) : null;
        return R.ok(reportService.daily(start, end));
    }

    /** 会员统计 */
    @PostMapping("/member-stat")
    public R<MemberStatVO> memberStat() {
        return R.ok(statService.memberStat());
    }

    /** 充值消费趋势 */
    @PostMapping("/trend")
    public R<List<TrendVO>> trend(@RequestBody Map<String, Object> body) {
        int days = body.get("days") != null ? Integer.parseInt(body.get("days").toString()) : 7;
        return R.ok(statService.trend(days));
    }

    /** 流失预警 */
    @PostMapping("/lost")
    public R<List<LostMemberVO>> lost(@RequestBody Map<String, Object> body) {
        int days = body.get("days") != null ? Integer.parseInt(body.get("days").toString()) : 30;
        return R.ok(statService.lost(days));
    }

    /** 销售排行：type=goods/service/clerk */
    @PostMapping("/sales-rank")
    public R<List<RankVO>> salesRank(@RequestBody Map<String, Object> body) {
        String type = body.get("type") != null ? body.get("type").toString() : "goods";
        int topN = body.get("topN") != null ? Integer.parseInt(body.get("topN").toString()) : 10;
        LocalDateTime start = body.get("startTime") != null ? LocalDateTime.parse(body.get("startTime").toString()) : null;
        LocalDateTime end = body.get("endTime") != null ? LocalDateTime.parse(body.get("endTime").toString()) : null;

        return switch (type) {
            case "service" -> R.ok(rankService.serviceRank(start, end, topN));
            case "clerk" -> R.ok(rankService.clerkRank(start, end, topN));
            default -> R.ok(rankService.goodsRank(start, end, topN));
        };
    }
}
