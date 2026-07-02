library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

entity sync_up_down_counter is
    Port ( 
        clk    : in  STD_LOGIC;  -- Clock input
        reset  : in  STD_LOGIC;  -- Active high reset
        mode   : in  STD_LOGIC;  -- Count direction (0 = Up, 1 = Down)
        count  : out STD_LOGIC_VECTOR (3 downto 0) -- 4-bit output
    );
end sync_up_down_counter;

architecture Behavioral of sync_up_down_counter is
    signal temp_count : STD_LOGIC_VECTOR (3 downto 0) := "0000";
begin
    process(clk, reset)
    begin
        if (reset = '1') then
            temp_count <= "0000";  -- Reset to 0
        elsif (rising_edge(clk)) then
            if (mode = '0') then
                temp_count <= temp_count + 1; -- Count Up
            else
                temp_count <= temp_count - 1; -- Count Down
            end if;
        end if;
    end process;
    
    count <= temp_count; -- Assign to output
end Behavioral;
