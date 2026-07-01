// d_flip_flop.sv - Design Module
module d_flip_flop (
    input  logic clk,     // Clock input using 4-state logic
    input  logic rst_n,   // Active-low asynchronous reset
    input  logic d,       // Data input
    output logic q        // Registered output
);

    // sequential logic block triggered by clock or reset edges
    always_ff @(posedge clk or negedge rst_n) begin
        if (!rst_n) begin
            q <= 1'b0;    // Clear output on reset
        end else begin
            q <= d;       // Latch data input on positive clock edge
        end
    end

endmodule
