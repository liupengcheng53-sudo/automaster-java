package com.automaster.controller;

import com.automaster.dto.ErrorResponse;
import com.automaster.entity.Car;
import com.automaster.repository.CarRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/cars")
@Tag(name = "车辆管理接口", description = "二手车的新增、查询、修改、删除接口，包含VIN校验、状态筛选等扩展功能")
public class CarController {

    private final CarRepository carRepository;

    @Autowired
    public CarController(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    /**
     * 查询所有车辆
     */
    @GetMapping
    @Operation(
            summary = "查询所有车辆",
            description = "获取数据库中所有车辆信息，包含完整的车辆属性",
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功，返回车辆列表",
                            content = @Content(schema = @Schema(implementation = Car.class))),
                    @ApiResponse(responseCode = "204", description = "暂无车辆数据", content = @Content),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
            }
    )
    public ResponseEntity<List<Car>> getAll() {
        List<Car> cars = carRepository.findAll();
        return cars.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(cars);
    }

    /**
     * 新增车辆
     */
    @PostMapping
    @Operation(
            summary = "新增车辆",
            description = "添加新车辆入库信息，包含VIN唯一性校验、预定状态客户/定金必填校验",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "车辆信息实体", required = true,
                    content = @Content(schema = @Schema(implementation = Car.class))),
            responses = {
                    @ApiResponse(responseCode = "201", description = "新增成功，返回新增的车辆信息",
                            content = @Content(schema = @Schema(implementation = Car.class))),
                    @ApiResponse(responseCode = "400", description = "参数错误（VIN重复/预定状态缺客户/定金等）",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "VIN重复",
                                                    summary = "VIN码重复错误",
                                                    value = "{\"code\":\"VIN_DUPLICATE\",\"message\":\"VIN码已存在，请检查后重新录入\"}"
                                            ),
                                            @ExampleObject(
                                                    name = "预定状态缺客户",
                                                    summary = "预定状态未关联客户",
                                                    value = "{\"code\":\"CUSTOMER_REQUIRED\",\"message\":\"预定状态必须关联客户\"}"
                                            )
                                    }
                            )),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
            }
    )
    public ResponseEntity<?> add(@RequestBody Car car) {
        // 1. 基础参数校验
        Map<String, String> errorResponse = new HashMap<>();
        if (car.getMake() == null || car.getMake().trim().isEmpty()) {
            errorResponse.put("code", "PARAM_ERROR");
            errorResponse.put("message", "品牌不能为空");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        if (car.getModel() == null || car.getModel().trim().isEmpty()) {
            errorResponse.put("code", "PARAM_ERROR");
            errorResponse.put("message", "型号不能为空");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        if (car.getYear() == null) {
            errorResponse.put("code", "PARAM_ERROR");
            errorResponse.put("message", "年份不能为空");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // 2. VIN重复校验
        Optional<Car> existingCar = carRepository.findByVin(car.getVin());
        if (existingCar.isPresent()) {
            errorResponse.put("code", "VIN_DUPLICATE");
            errorResponse.put("message", "VIN码已存在，请检查后重新录入");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // 3. 预定状态（PENDING）必填客户ID
        if ("PENDING".equals(car.getStatus()) && (car.getCustomerId() == null || car.getCustomerId().trim().isEmpty())) {
            errorResponse.put("code", "CUSTOMER_REQUIRED");
            errorResponse.put("message", "预定状态必须关联客户");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // 4. 预定状态必填定金
        if ("PENDING".equals(car.getStatus()) && (car.getDeposit() == null || car.getDeposit() <= 0)) {
            errorResponse.put("code", "DEPOSIT_REQUIRED");
            errorResponse.put("message", "预定状态必须填写有效定金金额");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        if (car.getCustomerId() == null || car.getCustomerId().trim().isEmpty()) {
            car.setCustomerId(null); // 空字符串→NULL
        }
        // 5. 正常保存
        Car savedCar = carRepository.save(car);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCar);
    }

    /**
     * 修改车辆信息
     */
    @PutMapping("/{id}")
    @Operation(
            summary = "修改车辆信息",
            description = "根据车辆ID更新车辆信息，包含VIN唯一性校验（排除自身）、预定状态客户/定金必填校验",
            parameters = {
                    @Parameter(name = "id", description = "车辆ID（UUID）", required = true,
                            example = "123e4567-e89b-12d3-a456-426614174000", schema = @Schema(type = "string"))
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "新的车辆信息", required = true,
                    content = @Content(schema = @Schema(implementation = Car.class))),
            responses = {
                    @ApiResponse(responseCode = "200", description = "修改成功，返回修改后的车辆信息",
                            content = @Content(schema = @Schema(implementation = Car.class))),
                    @ApiResponse(responseCode = "400", description = "参数错误（VIN重复/预定状态缺客户/定金等）",
                            content = @Content(
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "VIN重复",
                                                    value = "{\"code\":\"VIN_DUPLICATE\",\"message\":\"VIN码已存在，请检查后重新录入\"}"
                                            )
                                    }
                            )),
                    @ApiResponse(responseCode = "404", description = "车辆ID不存在",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = "{\"code\":\"CAR_NOT_FOUND\",\"message\":\"车辆ID不存在\"}"))),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
            }
    )
    public ResponseEntity<?> update(
            @Parameter(description = "车辆ID", required = true)
            @PathVariable String id,
            @Parameter(description = "新的车辆信息", required = true)
            @RequestBody Car car
    ) {
        // 1. 校验车辆是否存在
        if (!carRepository.existsById(id)) {
            Map<String, String> error = new HashMap<>();
            error.put("code", "CAR_NOT_FOUND");
            error.put("message", "车辆ID不存在");
            return ResponseEntity.notFound().build();
        }

        // 2. VIN重复校验（排除自身）
        Optional<Car> existingCar = carRepository.findByVin(car.getVin());
        if (existingCar.isPresent() && !existingCar.get().getId().equals(id)) {
            Map<String, String> error = new HashMap<>();
            error.put("code", "VIN_DUPLICATE");
            error.put("message", "VIN码已存在，请检查后重新录入");
            return ResponseEntity.badRequest().body(error);
        }

        // 3. 预定状态校验
        if ("PENDING".equals(car.getStatus())) {
            if (car.getCustomerId() == null || car.getCustomerId().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("code", "CUSTOMER_REQUIRED");
                error.put("message", "预定状态必须关联客户");
                return ResponseEntity.badRequest().body(error);
            }
            if (car.getDeposit() == null || car.getDeposit() <= 0) {
                Map<String, String> error = new HashMap<>();
                error.put("code", "DEPOSIT_REQUIRED");
                error.put("message", "预定状态必须填写有效定金金额");
                return ResponseEntity.badRequest().body(error);
            }
        }

        // 4. 正常更新
        car.setId(id);
        Car updatedCar = carRepository.save(car);
        return ResponseEntity.ok(updatedCar);
    }

    /**
     * 删除车辆
     */
    @DeleteMapping("/{id}")
    @Operation(
            summary = "删除车辆",
            description = "根据车辆ID删除车辆信息，删除后不可恢复，若车辆关联订单可能删除失败",
            parameters = {
                    @Parameter(name = "id", description = "车辆ID（UUID）", required = true,
                            example = "123e4567-e89b-12d3-a456-426614174000", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "删除成功，无返回内容", content = @Content),
                    @ApiResponse(responseCode = "404", description = "车辆ID不存在", content = @Content),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误（如车辆关联订单）", content = @Content)
            }
    )
    public ResponseEntity<Void> delete(
            @Parameter(description = "车辆ID", required = true)
            @PathVariable String id
    ) {
        if (!carRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        carRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 按状态查询车辆
     */
    @GetMapping("/by-status")
    @Operation(
            summary = "按状态查询车辆",
            description = "根据车辆状态筛选车辆（AVAILABLE/在售、SOLD/已售、PENDING/预定、MAINTENANCE/整备中）",
            parameters = {
                    @Parameter(name = "status", description = "车辆状态（英文枚举值）", required = true,
                            example = "AVAILABLE", schema = @Schema(type = "string",
                            allowableValues = {"AVAILABLE", "SOLD", "PENDING", "MAINTENANCE"}))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "查询成功，返回对应状态的车辆列表",
                            content = @Content(schema = @Schema(implementation = Car.class))),
                    @ApiResponse(responseCode = "204", description = "暂无对应状态的车辆数据", content = @Content),
                    @ApiResponse(responseCode = "400", description = "状态参数为空或不合法", content = @Content),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
            }
    )
    public ResponseEntity<List<Car>> getCarsByStatus(
            @Parameter(description = "车辆状态（AVAILABLE/SOLD/PENDING/MAINTENANCE）", required = true)
            @RequestParam String status
    ) {
        if (status == null || status.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        List<Car> cars = carRepository.findByStatus(status);
        return cars.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(cars);
    }

    /**
     * 按VIN查询（供前端校验）
     */
    @GetMapping("/check-vin")
    @Operation(
            summary = "VIN码唯一性校验",
            description = "校验VIN码是否已存在，新增场景无需传excludeId，编辑场景需传入车辆ID排除自身",
            parameters = {
                    @Parameter(name = "vin", description = "车辆VIN码", required = true,
                            example = "LFV3A23C993000000", schema = @Schema(type = "string")),
                    @Parameter(name = "excludeId", description = "排除的车辆ID（编辑场景使用）", required = false,
                            example = "123e4567-e89b-12d3-a456-426614174000", schema = @Schema(type = "string"))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "校验成功，返回VIN是否存在",
                            content = @Content(
                                    schema = @Schema(
                                            type = "object",
                                            description = "校验结果",
                                            properties = {
                                                    //@Schema(name = "exists", type = "boolean", example = "false")
                                            }
                                    ))),
                    @ApiResponse(responseCode = "400", description = "VIN参数为空", content = @Content),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
            }
    )
    public ResponseEntity<Map<String, Boolean>> checkVin(
            @Parameter(description = "车辆VIN码", required = true)
            @RequestParam String vin,
            @Parameter(description = "排除的车辆ID（编辑场景使用）", required = false)
            @RequestParam(required = false) String excludeId
    ) {
        Map<String, Boolean> result = new HashMap<>();
        Optional<Car> car = carRepository.findByVin(vin);
        if (excludeId != null) {
            // 编辑场景：排除自身ID
            result.put("exists", car.isPresent() && !car.get().getId().equals(excludeId));
        } else {
            // 新增场景
            result.put("exists", car.isPresent());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 将预定车辆变回在售状态
     * 删除客户和定金信息
     */
    @PutMapping("/{id}/back-to-sale")
    @Operation(
            summary = "预定车辆变回在售",
            description = "将预定状态的车辆变回在售状态，删除客户和定金信息",
            responses = {
                    @ApiResponse(responseCode = "200", description = "操作成功", content = @Content),
                    @ApiResponse(responseCode = "400", description = "车辆状态不是预定", content = @Content),
                    @ApiResponse(responseCode = "404", description = "车辆不存在", content = @Content),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
            }
    )
    public ResponseEntity<String> backToSale(
            @Parameter(description = "车辆ID", required = true)
            @PathVariable String id
    ) {
        try {
            Car car = carRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("车辆不存在"));

            // 检查车辆状态
            if (!"PENDING".equals(car.getStatus())) {
                return ResponseEntity.badRequest().body("只有预定状态的车辆才能变回在售");
            }

            // 变回在售状态，删除客户和定金信息
            car.setStatus("AVAILABLE");
            car.setCustomerId(null);
            car.setDeposit(null);

            carRepository.save(car);

            return ResponseEntity.ok("车辆已变回在售状态");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("操作失败：" + e.getMessage());
        }
    }

    /**
     * *
     * 完成预定，创建销售记录
     */
    @PutMapping("/{id}/complete-pending")
    @Operation(
            summary = "完成预定订单",
            description = "将预定车辆转为已售状态，创建销售交易记录",
            responses = {
                    @ApiResponse(responseCode = "200", description = "操作成功", content = @Content),
                    @ApiResponse(responseCode = "400", description = "车辆状态不是预定或参数错误", content = @Content),
                    @ApiResponse(responseCode = "404", description = "车辆不存在", content = @Content),
                    @ApiResponse(responseCode = "500", description = "服务器内部错误", content = @Content)
            }
    )
    public ResponseEntity<Map<String, Object>> completePending(
            @Parameter(description = "车辆ID", required = true)
            @PathVariable String id,
            @Parameter(description = "最终成交价", required = true)
            @RequestParam Integer finalPrice,
            @Parameter(description = "处理人员ID", required = false)
            @RequestParam(required = false) String handledByUserId
    ) {
        try {
            Car car = carRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("车辆不存在"));

            // 检查车辆状态
            if (!"PENDING".equals(car.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "只有预定状态的车辆才能完成销售"));
            }

            // 检查最终成交价
            if (finalPrice == null || finalPrice <= 0) {
                return ResponseEntity.badRequest().body(Map.of("message", "请输入有效的最终成交价"));
            }

            // 更新车辆状态为已售
            car.setStatus("SOLD");
            carRepository.save(car);

            // 返回成功信息，包含需要创建的交易记录数据
            Map<String, Object> result = new HashMap<>();
            result.put("message", "预定订单已完成销售");
            result.put("carId", car.getId());
            result.put("customerId", car.getCustomerId());
            result.put("deposit", car.getDeposit());
            result.put("finalPrice", finalPrice);
            result.put("handledByUserId", handledByUserId);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "操作失败：" + e.getMessage()));
        }
    }
}